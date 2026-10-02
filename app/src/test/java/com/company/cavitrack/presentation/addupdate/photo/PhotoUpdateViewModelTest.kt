package com.company.cavitrack.presentation.addupdate.photo

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.repository.AuthRepository
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.domain.repository.StorageRepository
import com.company.cavitrack.util.DataResult
import com.company.cavitrack.util.ImageUtil
import com.company.cavitrack.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUpdateViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: InventoryRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var storageRepository: StorageRepository

    private lateinit var viewModel: PhotoUpdateViewModel
    private lateinit var sampleFile: File

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ImageUtil)
        coEvery { ImageUtil.downscaleImage(any(), any()) } answers { firstArg() }

        repository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        storageRepository = mockk(relaxed = true)
        coEvery { storageRepository.deletePhoto(any()) } returns DataResult.Success(Unit)

        viewModel = PhotoUpdateViewModel(repository, authRepository, storageRepository, testDispatcher)
        sampleFile = tempFolder.newFile("test_photo_${System.currentTimeMillis()}.jpg")
    }

    @After
    fun tearDown() {
        unmockkObject(ImageUtil)
        Dispatchers.resetMain()
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when user is unauthenticated sets error state`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns null

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = viewModel.error.value
        assertNotNull(error)
        assertTrue(error is UiText.DynamicString)
        assertEquals("User not authenticated.", (error as UiText.DynamicString).value)
        coVerify(exactly = 0) { storageRepository.uploadPhoto(any(), any()) }
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when storage upload fails sets error state`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns "user-123"
        coEvery { storageRepository.uploadPhoto(any(), any()) } returns DataResult.Error("Network error")

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = viewModel.error.value
        assertNotNull(error)
        assertTrue(error is UiText.DynamicString)
        assertEquals("Network error", (error as UiText.DynamicString).value)
        coVerify(exactly = 0) { repository.saveComponentWithHistory(any(), any()) }
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when Component update succeeds emits isSaved`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns "user-123"
        every { authRepository.getCurrentUserName() } returns "John Doe"
        coEvery { storageRepository.uploadPhoto(any(), any()) } returns DataResult.Success("https://storage.com/new.jpg")

        val existingComp = Component(
            id = "comp-1", name = "Widget", sku = "W1", category = "General",
            qty = 10, minStockThreshold = 2, unit = "pcs", photoUrl = null,
            createdAt = 1L, updatedAt = 1L
        )
        coEvery { repository.getComponent("comp-1") } returns DataResult.Success(existingComp)
        coEvery { repository.saveComponentWithHistory(any(), any()) } returns DataResult.Success(Unit)

        var isSavedEmitted = false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isSaved.collect { isSavedEmitted = true }
        }

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(isSavedEmitted)
        coVerify { repository.saveComponentWithHistory(match { it.photoUrl == "https://storage.com/new.jpg" }, any()) }
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when repository save fails rolls back uploaded photo`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns "user-123"
        coEvery { storageRepository.uploadPhoto(any(), any()) } returns DataResult.Success("https://storage.com/new.jpg")

        val existingComp = Component(
            id = "comp-1", name = "Widget", sku = "W1", category = "General",
            qty = 10, minStockThreshold = 2, unit = "pcs", photoUrl = null,
            createdAt = 1L, updatedAt = 1L
        )
        coEvery { repository.getComponent("comp-1") } returns DataResult.Success(existingComp)
        coEvery { repository.saveComponentWithHistory(any(), any()) } returns DataResult.Error("Firestore write rejected")

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = viewModel.error.value
        assertNotNull(error)
        assertEquals("Firestore write rejected", (error as UiText.DynamicString).value)
        coVerify(exactly = 1) { storageRepository.deletePhoto("https://storage.com/new.jpg") }
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when repository throws exception rolls back uploaded photo`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns "user-123"
        coEvery { storageRepository.uploadPhoto(any(), any()) } returns DataResult.Success("https://storage.com/new.jpg")
        coEvery { repository.getComponent("comp-1") } throws RuntimeException("Unexpected DB crash")

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = viewModel.error.value
        assertNotNull(error)
        assertEquals("Unexpected DB crash", (error as UiText.DynamicString).value)
        coVerify(exactly = 1) { storageRepository.deletePhoto("https://storage.com/new.jpg") }
    }

    @Test
    fun `uploadPhotoAndUpdateEntity when previous photo existed deletes old photo after successful save`() = runTest {
        // Arrange
        every { authRepository.getCurrentUserUid() } returns "user-123"
        coEvery { storageRepository.uploadPhoto(any(), any()) } returns DataResult.Success("https://storage.com/new.jpg")

        val existingComp = Component(
            id = "comp-1", name = "Widget", sku = "W1", category = "General",
            qty = 10, minStockThreshold = 2, unit = "pcs", photoUrl = "https://storage.com/old.jpg",
            createdAt = 1L, updatedAt = 1L
        )
        coEvery { repository.getComponent("comp-1") } returns DataResult.Success(existingComp)
        coEvery { repository.saveComponentWithHistory(any(), any()) } returns DataResult.Success(Unit)

        // Act
        viewModel.uploadPhotoAndUpdateEntity(EntityType.Component, "comp-1", sampleFile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { storageRepository.deletePhoto("https://storage.com/old.jpg") }
    }
}
