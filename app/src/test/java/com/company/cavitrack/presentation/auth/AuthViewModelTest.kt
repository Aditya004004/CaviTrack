package com.company.cavitrack.presentation.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.company.cavitrack.domain.usecase.auth.AuthUseCases
import com.company.cavitrack.util.SessionManager
import com.company.cavitrack.util.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authUseCases: AuthUseCases
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authUseCases = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        
        every { authUseCases.getCurrentUserUid() } returns null
        every { sessionManager.currentUser } returns MutableStateFlow(null)

        viewModel = AuthViewModel(authUseCases, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates state to Authenticated`() = runTest(testDispatcher) {
        // Arrange
        coEvery { authUseCases.login(any(), any()) } returns DataResult.Success(Unit)

        // Act
        viewModel.login("test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.authState.value is AuthState.Authenticated)
        coVerify { authUseCases.login(any(), any()) }
    }

    @Test
    fun `login error updates state to Error`() = runTest(testDispatcher) {
        // Arrange
        val errorMsg = "Login failed"
        coEvery { authUseCases.login(any(), any()) } returns DataResult.Error(errorMsg)

        // Act
        viewModel.login("test@test.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.authState.value is AuthState.Error)
        assertTrue((viewModel.authState.value as AuthState.Error).message == errorMsg)
    }

    @Test
    fun `logout updates state to Unauthenticated`() = runTest(testDispatcher) {
        // Act
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.authState.value is AuthState.Unauthenticated)
        coVerify { authUseCases.logout() }
    }

    @Test
    fun `sessionManager user null emission transitions to Unauthenticated`() = runTest(testDispatcher) {
        val userFlow = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(mockk())
        every { sessionManager.currentUser } returns userFlow

        val testVm = AuthViewModel(authUseCases, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        userFlow.value = null
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(testVm.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `uiText StringResource value equality works for matching args`() {
        val text1 = com.company.cavitrack.util.UiText.StringResource(101, "abc", 99)
        val text2 = com.company.cavitrack.util.UiText.StringResource(101, "abc", 99)
        val text3 = com.company.cavitrack.util.UiText.StringResource(101, "different")

        assertEquals(text1, text2)
        assertEquals(text1.hashCode(), text2.hashCode())
        assertNotEquals(text1, text3)
    }

    @Test
    fun `sendPasswordReset delegates to authUseCases and returns result`() = runTest(testDispatcher) {
        coEvery { authUseCases.sendPasswordResetEmail("reset@example.com") } returns DataResult.Success(Unit)

        var result: DataResult<Unit>? = null
        viewModel.sendPasswordReset("reset@example.com") { result = it }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result is DataResult.Success)
        coVerify { authUseCases.sendPasswordResetEmail("reset@example.com") }
    }
}
