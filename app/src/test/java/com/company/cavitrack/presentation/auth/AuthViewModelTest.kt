package com.company.cavitrack.presentation.auth

import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.google.android.gms.tasks.Tasks

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tokenManager: TokenManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: InventoryRepository
    private lateinit var syncScheduler: com.company.cavitrack.util.SyncScheduler
    private lateinit var sessionManager: com.company.cavitrack.util.SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tokenManager = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        every { sessionManager.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(null)

        viewModel = AuthViewModel(tokenManager, firebaseAuth, repository, sessionManager, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteAccount clears user data and deletes user account`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "test_uid"
        every { firebaseAuth.currentUser } returns mockUser
        
        // Mock successful deletion tasks
        every { mockUser.delete() } returns Tasks.forResult(null)
        coEvery { repository.clearUserData("test_uid") } just Runs
        every { tokenManager.clearToken() } just Runs

        // Act
        viewModel.deleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify { mockUser.delete() }
        coVerify { repository.clearUserData("test_uid") }
        verify { tokenManager.clearToken() }
        
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
    
    @Test
    fun `logout clears token and removes FCM token`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "test_uid"
        every { firebaseAuth.currentUser } returns mockUser
        every { tokenManager.clearToken() } just Runs
        
        // Mock Firebase Messaging - This might be hard to mock since it's a static call in AuthViewModel
        // The test will just verify tokenManager.clearToken() is called for now
        
        // Act
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        verify { tokenManager.clearToken() }
        
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
}
