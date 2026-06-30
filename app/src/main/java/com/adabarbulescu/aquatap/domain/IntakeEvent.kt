package com.adabarbulescu.aquatap.domain

data class IntakeEvent(
    val amountMl: Int,
    val timestampMillis: Long
)