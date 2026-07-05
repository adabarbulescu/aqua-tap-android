package com.adabarbulescu.aquatap.data

import kotlinx.coroutines.flow.Flow

interface IntakeRepository {
    fun observeEventsForDay(start: Long, end: Long): Flow<List<IntakeEventEntity>>
    suspend fun insertEvent(event: IntakeEventEntity)
    suspend fun clearDay(start: Long, end: Long)
}

class RoomIntakeRepository(private val intakeDao: IntakeDao) : IntakeRepository {
    override fun observeEventsForDay(start: Long, end: Long): Flow<List<IntakeEventEntity>> {
        return intakeDao.observeEventsForDay(start, end)
    }

    override suspend fun insertEvent(event: IntakeEventEntity) {
        intakeDao.insert(event)
    }

    override suspend fun clearDay(start: Long, end: Long) {
        intakeDao.clearDay(start, end)
    }
}
