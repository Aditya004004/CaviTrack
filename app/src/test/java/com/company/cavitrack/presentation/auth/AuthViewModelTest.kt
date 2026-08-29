package com.company.cavitrack.presentation.auth




import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.company.cavitrack.domain.repository.InventoryRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coVerify
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

import com.company.cavitrack.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUserMetadata

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: InventoryRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var syncScheduler: com.company.cavitrack.util.SyncScheduler
    private lateinit var sessionManager: com.company.cavitrack.util.SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        firebaseAuth = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        every { sessionManager.currentUser } returns MutableStateFlow(null)

        viewModel = AuthViewModel(firebaseAuth, authRepository, repository, sessionManager, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteAccount clears user data and deletes user account`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val mockMetadata = mockk<FirebaseUserMetadata>(relaxed = true)
        every { mockMetadata.lastSignInTimestamp } returns System.currentTimeMillis()
        every { mockUser.uid } returns "test_uid"
        every { mockUser.metadata } returns mockMetadata
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { repository.clearUserData("test_uid") } just Runs
        coEvery { authRepository.clearStorage() } just Runs
        coEvery { authRepository.deleteAccount() } just Runs
        every { firebaseAuth.signOut() } just Runs

        // Act
        viewModel.deleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { repository.clearUserData("test_uid") }
        coVerify { authRepository.clearStorage() }
        coVerify { authRepository.deleteAccount() }
        verify { firebaseAuth.signOut() }
        
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
    
    @Test
    fun `deleteAccount blocks if recent login is required`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val mockMetadata = mockk<FirebaseUserMetadata>(relaxed = true)
        every { mockMetadata.lastSignInTimestamp } returns System.currentTimeMillis() - (10 * 60 * 1000) // 10 minutes ago
        every { mockUser.uid } returns "test_uid"
        every { mockUser.metadata } returns mockMetadata
        every { firebaseAuth.currentUser } returns mockUser

        // Act
        viewModel.deleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        // Should not call data clearing
        coVerify(exactly = 0) { repository.clearUserData(any()) }
        coVerify(exactly = 0) { authRepository.deleteAccount() }
        
        val error = viewModel.authError.value
        assertTrue(error?.contains("Recent login required") == true)
    }

    @Test
    fun `logout clears token and removes FCM token`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "test_uid"
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { authRepository.clearFcmToken() } just Runs
        every { firebaseAuth.signOut() } just Runs
        
        // Act
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        coVerify { authRepository.clearFcmToken() }
        verify { firebaseAuth.signOut() }
        
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
}





