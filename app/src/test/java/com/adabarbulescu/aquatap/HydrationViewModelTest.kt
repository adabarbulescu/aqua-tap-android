package com.adabarbulescu.aquatap

import com.adabarbulescu.aquatap.data.BottleTagRepository
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
    
    private class FakeRepository : BottleTagRepository {
        private val _pairedId = MutableStateFlow<String?>(null)
        override val pairedTagId: Flow<String?> = _pairedId

        override suspend fun savePairedTagId(tagId: String) {
            _pairedId.value = tagId
        }

        override suspend fun clearPairedTagId() {
            _pairedId.value = null
        }
    }

    private lateinit var viewModel: HydrationViewModel
    private lateinit var repository: FakeRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRepository()
        viewModel = HydrationViewModel(repository)
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
        repository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.handleNfcScan("TAG123")
        advanceUntilIdle()
        
        assertEquals(250, viewModel.state.value.totalIntakeMl)
    }

    @Test
    fun scanningWrongTag_doesNotRecordIntake() = runTest {
        repository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.handleNfcScan("WRONG_TAG")
        advanceUntilIdle()
        
        assertEquals(0, viewModel.state.value.totalIntakeMl)
    }

    @Test
    fun unpairing_clearsTagId() = runTest {
        repository.savePairedTagId("TAG123")
        advanceUntilIdle()
        
        viewModel.unpairBottle()
        advanceUntilIdle()
        
        assertNull(viewModel.state.value.pairedTagId)
    }
}
