package com.company.cavitrack.presentation.auth




import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.TokenManager
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tokenManager: TokenManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: InventoryRepository
    private lateinit var syncScheduler: com.company.cavitrack.util.SyncScheduler
    private lateinit var sessionManager: com.company.cavitrack.util.SessionManager
    private lateinit var firebaseFirestore: com.google.firebase.firestore.FirebaseFirestore
    private lateinit var firebaseStorage: com.google.firebase.storage.FirebaseStorage
    private lateinit var firebaseMessaging: com.google.firebase.messaging.FirebaseMessaging
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tokenManager = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        firebaseFirestore = mockk(relaxed = true)
        firebaseStorage = mockk(relaxed = true)
        firebaseMessaging = mockk(relaxed = true)
        every { sessionManager.currentUser } returns MutableStateFlow(null)

        viewModel = AuthViewModel(tokenManager, firebaseAuth, repository, sessionManager, syncScheduler, firebaseFirestore, firebaseStorage, firebaseMessaging)
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
        
        every { firebaseMessaging.token } returns Tasks.forResult("mock_fcm_token")
        every { firebaseMessaging.deleteToken() } returns Tasks.forResult(null)
        val mockDocument = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { mockDocument.delete() } returns Tasks.forResult(null)
        val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        every { firebaseFirestore.collection("users").document("test_uid").collection("fcmTokens") } returns mockCollection
        every { mockCollection.document("mock_fcm_token") } returns mockDocument
        
        // Act
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assert
        verify { tokenManager.clearToken() }
        verify { firebaseMessaging.deleteToken() }
        verify { mockDocument.delete() }
        
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Unauthenticated)
    }
}





