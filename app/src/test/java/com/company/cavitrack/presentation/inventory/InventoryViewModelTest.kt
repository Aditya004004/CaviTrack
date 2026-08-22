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
import org.junit.Assert.assertTrue
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
            Component(id = "1", name = "Screw", sku = "SKU1", category = "Fastener", qty = 100, unit = "pcs", minStockThreshold = 50)
        )
        every { repository.getComponents() } returns flowOf(Result.Success(components))
        every { repository.getCustomers() } returns flowOf(Result.Success(emptyList()))
        every { repository.getMolds() } returns flowOf(Result.Success(emptyList()))

        // Act
        viewModel = InventoryViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val successState = state as UiState.Success
        assertEquals(1, successState.data.components.size)
        assertEquals("Screw", successState.data.components[0].name)
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
        assertTrue(state is UiState.Error)
        assertEquals("Network Error", (state as UiState.Error).message)
    }
}
