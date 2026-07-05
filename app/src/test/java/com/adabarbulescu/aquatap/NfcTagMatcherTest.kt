package com.adabarbulescu.aquatap

import com.adabarbulescu.aquatap.domain.NfcTagMatcher
import com.adabarbulescu.aquatap.domain.TagMatchResult
import org.junit.Assert.assertEquals
import org.junit.Test

class NfcTagMatcherTest {

    @Test
    fun matches_returnsMatchWhenIdsAreEqual() {
        assertEquals(TagMatchResult.Match, NfcTagMatcher.matches("TAG123", "TAG123"))
    }

    @Test
    fun matches_returnsMismatchWhenIdsAreDifferent() {
        assertEquals(TagMatchResult.Mismatch, NfcTagMatcher.matches("TAG123", "TAG456"))
    }

    @Test
    fun matches_returnsNoTagPairedWhenPairedIdIsNull() {
        assertEquals(TagMatchResult.NoTagPaired, NfcTagMatcher.matches("TAG123", null))
    }
}
