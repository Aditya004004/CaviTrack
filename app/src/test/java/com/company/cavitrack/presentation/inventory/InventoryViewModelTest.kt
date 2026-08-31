package com.company.cavitrack.presentation.inventory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.util.SessionManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import androidx.paging.PagingData

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private lateinit var viewModel: InventoryViewModel
    private val useCases: InventoryUseCases = mockk(relaxed = true)
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
    fun `viewModel exposes paging flows from useCases`() = runTest {
        // Arrange
        every { useCases.getComponents() } returns flowOf(PagingData.empty())
        every { useCases.getCustomers() } returns flowOf(PagingData.empty())
        every { useCases.getMolds() } returns flowOf(PagingData.empty())
        
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true) {
            every { uid } returns "123"
        }
        val sessionManager = mockk<SessionManager> {
            every { currentUser } returns MutableStateFlow(mockUser)
        }

        // Act
        viewModel = InventoryViewModel(useCases, sessionManager)

        // Assert
        assertNotNull(viewModel.componentsFlow)
        assertNotNull(viewModel.customersFlow)
        assertNotNull(viewModel.moldsFlow)
    }
}
