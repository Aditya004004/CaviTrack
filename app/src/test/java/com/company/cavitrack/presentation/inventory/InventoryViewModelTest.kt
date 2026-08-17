package com.company.cavitrack.presentation.inventory

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.Result
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private lateinit var viewModel: InventoryViewModel
    private val repository: InventoryRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData success updates uiState with data`() = runTest {
        // Arrange
        val components = listOf(
            Component("1", "Screw", "SKU1", "Fastener", 100, "pcs", 50, emptyList(), null, 0L, 0L)
        )
        every { repository.getComponents() } returns flowOf(Result.Success(components))
        every { repository.getCustomers() } returns flowOf(Result.Success(emptyList()))
        every { repository.getMolds() } returns flowOf(Result.Success(emptyList()))

        // Act
        viewModel = InventoryViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
        assertEquals(1, state.data?.components?.size)
        assertEquals("Screw", state.data?.components?.get(0)?.name)
    }

    @Test
    fun `loadData error updates uiState with error message`() = runTest {
        // Arrange
        every { repository.getComponents() } returns flowOf(Result.Error("Network Error"))
        every { repository.getCustomers() } returns flowOf(Result.Success(emptyList()))
        every { repository.getMolds() } returns flowOf(Result.Success(emptyList()))

        // Act
        viewModel = InventoryViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Network Error", state.error)
        assertEquals(null, state.data)
    }
}
