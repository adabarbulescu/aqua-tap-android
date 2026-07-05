package com.adabarbulescu.aquatap.domain

data class HydrationState(
    val totalIntakeMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val history: List<IntakeEvent> = emptyList(),
    val pairedTagId: String? = null,
    val isPairingEnabled: Boolean = false
)
