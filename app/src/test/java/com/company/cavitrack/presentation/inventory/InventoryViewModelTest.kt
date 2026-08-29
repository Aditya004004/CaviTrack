package com.company.cavitrack.presentation.inventory



import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import io.mockk.every
import io.mockk.mockk
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
        every { repository.getComponents() } returns flowOf(DataResult.Success(components))
        every { repository.getCustomers() } returns flowOf(DataResult.Success(emptyList()))
        every { repository.getMolds() } returns flowOf(DataResult.Success(emptyList()))
        
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true) {
            every { uid } returns "123"
        }
        val sessionManager = mockk<com.company.cavitrack.util.SessionManager> {
            every { currentUser } returns MutableStateFlow(mockUser)
        }

        // Act
        viewModel = InventoryViewModel(repository, sessionManager)
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
        every { repository.getComponents() } returns flowOf(DataResult.Error("Network Error"))
        every { repository.getCustomers() } returns flowOf(DataResult.Success(emptyList()))
        every { repository.getMolds() } returns flowOf(DataResult.Success(emptyList()))

        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true) {
            every { uid } returns "123"
        }
        val sessionManager = mockk<com.company.cavitrack.util.SessionManager> {
            every { currentUser } returns MutableStateFlow(mockUser)
        }

        // Act
        viewModel = InventoryViewModel(repository, sessionManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network Error", (state as UiState.Error).message)
    }
}



