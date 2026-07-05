package com.adabarbulescu.aquatap.domain

object NfcTagMatcher {
    fun matches(scannedId: String, pairedId: String?): TagMatchResult {
        return when {
            pairedId == null -> TagMatchResult.NoTagPaired
            scannedId == pairedId -> TagMatchResult.Match
            else -> TagMatchResult.Mismatch
        }
    }
}

sealed class TagMatchResult {
    data object Match : TagMatchResult()
    data object Mismatch : TagMatchResult()
    data object NoTagPaired : TagMatchResult()
}
