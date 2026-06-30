package com.adabarbulescu.aquatap

import com.adabarbulescu.aquatap.domain.HydrationCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class HydrationCalculatorTest {

    @Test
    fun remaining_returnsDifferenceWhenGoalIsNotReached() {
        assertEquals(750, HydrationCalculator.remaining(1250, 2000))
    }

    @Test
    fun remaining_neverReturnsNegativeValue() {
        assertEquals(0, HydrationCalculator.remaining(2500, 2000))
    }

    @Test
    fun progress_returnsFractionBetweenZeroAndOne() {
        assertEquals(0.5f, HydrationCalculator.progress(1000, 2000), 0.001f)
    }

    @Test
    fun progress_isCappedAtOne() {
        assertEquals(1f, HydrationCalculator.progress(2500, 2000), 0.001f)
    }

    @Test
    fun progress_returnsZeroWhenGoalIsInvalid() {
        assertEquals(0f, HydrationCalculator.progress(1000, 0), 0.001f)
    }
}
