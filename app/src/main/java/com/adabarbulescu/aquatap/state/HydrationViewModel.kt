package com.adabarbulescu.aquatap.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adabarbulescu.aquatap.data.BottleTagRepository
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

class HydrationViewModel(private val repository: BottleTagRepository) : ViewModel() {

    private val _state = MutableStateFlow(HydrationState())
    val state: StateFlow<HydrationState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        repository.pairedTagId
            .onEach { pairedId ->
                _state.update { it.copy(pairedTagId = pairedId) }
            }
            .launchIn(viewModelScope)
    }

    fun togglePairingMode(enabled: Boolean) {
        _state.update { it.copy(isPairingEnabled = enabled) }
    }

    fun handleNfcScan(tagId: String) {
        val current = _state.value
        
        if (current.isPairingEnabled && current.pairedTagId == null) {
            viewModelScope.launch {
                repository.savePairedTagId(tagId)
                _state.update { it.copy(isPairingEnabled = false) }
                _events.emit(UiEvent.BottlePaired)
            }
            return
        }

        when (NfcTagMatcher.matches(tagId, current.pairedTagId)) {
            TagMatchResult.Match -> {
                recordIntake()
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
            repository.clearPairedTagId()
        }
    }

    fun recordIntake(amountMl: Int = DEFAULT_SCAN_AMOUNT_ML) {
        if (amountMl <= 0) return

        _state.update { current ->
            current.copy(
                totalIntakeMl = current.totalIntakeMl + amountMl,
                history = listOf(
                    IntakeEvent(
                        amountMl = amountMl,
                        timestampMillis = System.currentTimeMillis()
                    )
                ) + current.history.take(MAX_HISTORY_ITEMS - 1)
            )
        }
    }

    fun resetDailyProgress() {
        _state.update { current ->
            HydrationState(
                dailyGoalMl = current.dailyGoalMl,
                pairedTagId = current.pairedTagId,
                isPairingEnabled = current.isPairingEnabled
            )
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
                val repository = com.adabarbulescu.aquatap.data.DataStoreBottleTagRepository(application.applicationContext)
                return HydrationViewModel(repository) as T
            }
        }
    }
}
