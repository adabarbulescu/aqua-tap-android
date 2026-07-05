package com.adabarbulescu.aquatap.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Query("SELECT * FROM intake_events WHERE timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC")
    fun observeEventsForDay(start: Long, end: Long): Flow<List<IntakeEventEntity>>

    @Insert
    suspend fun insert(event: IntakeEventEntity)

    @Query("DELETE FROM intake_events WHERE timestampMillis BETWEEN :start AND :end")
    suspend fun clearDay(start: Long, end: Long)
}
