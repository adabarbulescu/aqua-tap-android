package com.adabarbulescu.aquatap.state

import androidx.lifecycle.ViewModel
import com.adabarbulescu.aquatap.domain.HydrationState
import com.adabarbulescu.aquatap.domain.IntakeEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HydrationViewModel : ViewModel() {

    private val _state = MutableStateFlow(HydrationState())
    val state: StateFlow<HydrationState> = _state.asStateFlow()

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
        _state.value = HydrationState(
            dailyGoalMl = _state.value.dailyGoalMl
        )
    }

    companion object {
        const val DEFAULT_SCAN_AMOUNT_ML = 250
        private const val MAX_HISTORY_ITEMS = 5
    }
}
