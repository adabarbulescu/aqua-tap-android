package com.adabarbulescu.aquatap.domain

object HydrationCalculator {

    fun remaining(totalIntakeMl: Int, dailyGoalMl: Int): Int {
        return (dailyGoalMl - totalIntakeMl).coerceAtLeast(0)
    }

    fun progress(totalIntakeMl: Int, dailyGoalMl: Int): Float {
        if (dailyGoalMl <= 0) return 0f
        return (totalIntakeMl.toFloat() / dailyGoalMl).coerceIn(0f, 1f)
    }
}