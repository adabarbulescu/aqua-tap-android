package com.adabarbulescu.aquatap.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "intake_events")
data class IntakeEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestampMillis: Long,
    val source: String // "nfc" or "simulated"
)
