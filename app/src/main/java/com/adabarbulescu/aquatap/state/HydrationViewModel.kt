package com.adabarbulescu.aquatap.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adabarbulescu.aquatap.data.AquaTapDatabase
import com.adabarbulescu.aquatap.data.DataStoreSettingsRepository
import com.adabarbulescu.aquatap.data.IntakeEventEntity
import com.adabarbulescu.aquatap.data.IntakeRepository
import com.adabarbulescu.aquatap.data.RoomIntakeRepository
import com.adabarbulescu.aquatap.data.SettingsRepository
import com.adabarbulescu.aquatap.domain.DailyHydrationSummary
import com.adabarbulescu.aquatap.domain.HydrationState
import com.adabarbulescu.aquatap.domain.IntakeEvent
import com.adabarbulescu.aquatap.domain.NfcTagMatcher
import com.adabarbulescu.aquatap.domain.TagMatchResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HydrationViewModel(
    private val settingsRepository: SettingsRepository,
    private val intakeRepository: IntakeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HydrationState())
    val state: StateFlow<HydrationState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        // Observe paired tag
        settingsRepository.pairedTagId
            .onEach { pairedId ->
                _state.update { it.copy(pairedTagId = pairedId) }
            }
            .launchIn(viewModelScope)

        // Observe daily goal
        settingsRepository.dailyGoalMl
            .onEach { goal ->
                _state.update { it.copy(dailyGoalMl = goal) }
            }
            .launchIn(viewModelScope)

        // Observe today's intake
        val (start, end) = DailyHydrationSummary.getTodayRange()
        intakeRepository.observeEventsForDay(start, end)
            .onEach { entities ->
                val total = entities.sumOf { it.amountMl }
                val history = entities.map { 
                    IntakeEvent(it.amountMl, it.timestampMillis) 
                }.take(MAX_HISTORY_ITEMS)
                
                _state.update { it.copy(
                    totalIntakeMl = total,
                    history = history
                ) }
            }
            .launchIn(viewModelScope)
    }

    fun togglePairingMode(enabled: Boolean) {
        _state.update { it.copy(isPairingEnabled = enabled) }
    }

    fun updateDailyGoal(goalMl: Int) {
        if (goalMl < 100) return // Reasonable minimum
        viewModelScope.launch {
            settingsRepository.updateDailyGoal(goalMl)
        }
    }

    fun handleNfcScan(tagId: String, isSimulated: Boolean = false) {
        val current = _state.value
        
        if (current.isPairingEnabled && current.pairedTagId == null) {
            viewModelScope.launch {
                settingsRepository.savePairedTagId(tagId)
                _state.update { it.copy(isPairingEnabled = false) }
                _events.emit(UiEvent.BottlePaired)
            }
            return
        }

        when (NfcTagMatcher.matches(tagId, current.pairedTagId)) {
            TagMatchResult.Match -> {
                recordIntake(isSimulated = isSimulated)
                viewModelScope.launch { _events.emit(UiEvent.IntakeRecorded) }
            }
            TagMatchResult.Mismatch -> {
                viewModelScope.launch { _events.emit(UiEvent.WrongBottle) }
            }
            TagMatchResult.NoTagPaired -> {
                viewModelScope.launch { _events.emit(UiEvent.NoBottlePaired) }
            }
        }
    }

    fun unpairBottle() {
        viewModelScope.launch {
            settingsRepository.clearPairedTagId()
        }
    }

    fun recordIntake(amountMl: Int = DEFAULT_SCAN_AMOUNT_ML, isSimulated: Boolean = false) {
        if (amountMl <= 0) return

        viewModelScope.launch {
            intakeRepository.insertEvent(
                IntakeEventEntity(
                    amountMl = amountMl,
                    timestampMillis = System.currentTimeMillis(),
                    source = if (isSimulated) "simulated" else "nfc"
                )
            )
        }
    }

    fun resetDailyProgress() {
        val (start, end) = DailyHydrationSummary.getTodayRange()
        viewModelScope.launch {
            intakeRepository.clearDay(start, end)
        }
    }

    sealed class UiEvent {
        data object IntakeRecorded : UiEvent()
        data object BottlePaired : UiEvent()
        data object WrongBottle : UiEvent()
        data object NoBottlePaired : UiEvent()
    }

    companion object {
        const val DEFAULT_SCAN_AMOUNT_ML = 250
        private const val MAX_HISTORY_ITEMS = 5

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val context = application.applicationContext
                
                val settingsRepo = DataStoreSettingsRepository(context)
                val database = AquaTapDatabase.getDatabase(context)
                val intakeRepo = RoomIntakeRepository(database.intakeDao())
                
                return HydrationViewModel(settingsRepo, intakeRepo) as T
            }
        }
    }
}
