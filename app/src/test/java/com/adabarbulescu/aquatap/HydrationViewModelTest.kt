package com.adabarbulescu.aquatap

import com.adabarbulescu.aquatap.data.BottleTagRepository
import com.adabarbulescu.aquatap.data.IntakeEventEntity
import com.adabarbulescu.aquatap.data.IntakeRepository
import com.adabarbulescu.aquatap.state.HydrationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HydrationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private class FakeBottleRepository : BottleTagRepository {
        private val _pairedId = MutableStateFlow<String?>(null)
        override val pairedTagId: Flow<String?> = _pairedId

        override suspend fun savePairedTagId(tagId: String) {
            _pairedId.value = tagId
        }

        override suspend fun clearPairedTagId() {
            _pairedId.value = null
        }
    }

    private class FakeIntakeRepository : IntakeRepository {
        private val _events = MutableStateFlow<List<IntakeEventEntity>>(emptyList())
        override fun observeEventsForDay(start: Long, end: Long): Flow<List<IntakeEventEntity>> = _events

        override suspend fun insertEvent(event: IntakeEventEntity) {
            _events.value = _events.value + event
        }

        override suspend fun clearDay(start: Long, end: Long) {
            _events.value = emptyList()
        }
    }

    private lateinit var viewModel: HydrationViewModel
    private lateinit var bottleRepository: FakeBottleRepository
    private lateinit var intakeRepository: FakeIntakeRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bottleRepository = FakeBottleRepository()
        intakeRepository = FakeIntakeRepository()
        viewModel = HydrationViewModel(bottleRepository, intakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasNoPairedTag() = runTest {
        advanceUntilIdle()
        assertNull(viewModel.state.value.pairedTagId)
    }

    @Test
    fun pairingTag_updatesState() = runTest {
        viewModel.togglePairingMode(true)
        viewModel.handleNfcScan("TAG123")
        advanceUntilIdle()
        
        assertEquals("TAG123", viewModel.state.value.pairedTagId)
        assertEquals(false, viewModel.state.value.isPairingEnabled)
    }

    @Test
    fun scanningPairedTag_recordsIntake() = runTest {
        // Pair first
        bottleRepository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.handleNfcScan("TAG123")
        advanceUntilIdle()
        
        assertEquals(250, viewModel.state.value.totalIntakeMl)
        assertEquals(1, viewModel.state.value.history.size)
    }

    @Test
    fun scanningWrongTag_doesNotRecordIntake() = runTest {
        bottleRepository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.handleNfcScan("WRONG_TAG")
        advanceUntilIdle()
        
        assertEquals(0, viewModel.state.value.totalIntakeMl)
    }

    @Test
    fun unpairing_clearsTagId() = runTest {
        bottleRepository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.unpairBottle()
        advanceUntilIdle()
        
        assertNull(viewModel.state.value.pairedTagId)
    }

    @Test
    fun resetDailyProgress_clearsHistory() = runTest {
        bottleRepository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.handleNfcScan("TAG123")
        advanceUntilIdle()
        
        assertEquals(250, viewModel.state.value.totalIntakeMl)
        
        viewModel.resetDailyProgress()
        advanceUntilIdle()
        
        assertEquals(0, viewModel.state.value.totalIntakeMl)
        assertEquals(0, viewModel.state.value.history.size)
    }
}
