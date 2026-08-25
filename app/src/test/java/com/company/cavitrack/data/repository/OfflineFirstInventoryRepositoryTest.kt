package com.company.cavitrack.data.repository

import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstInventoryRepositoryTest {

    private lateinit var dao: InventoryDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var syncScheduler: com.company.cavitrack.util.SyncScheduler
    private lateinit var repository: OfflineFirstInventoryRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)

        repository = OfflineFirstInventoryRepository(
            dao = dao,
            firestore = firestore,
            firebaseAuth = firebaseAuth,
            syncScheduler = syncScheduler
        )
    }

    @Test
    fun `saveComponent returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val component = Component(id = "1", name = "Test", sku = "SKU1", category = "Cat", minStockThreshold = 10, unit = "pcs", ownerId = "")

        // Act
        val result = repository.saveComponent(component)

        // Assert
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Must be authenticated"))
        coVerify(exactly = 0) { dao.insertComponent(any()) }
    }

    @Test
    fun `saveComponent inserts to DAO and queues action when authenticated`() = runTest {
        // Arrange
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "test_uid"
        every { firebaseAuth.currentUser } returns mockUser
        
        coEvery { dao.getComponent("1", "test_uid") } returns null
        
        val component = Component(id = "1", name = "Test", sku = "SKU1", category = "Cat", minStockThreshold = 10, unit = "pcs", ownerId = "test_uid")

        // Act
        val result = repository.saveComponent(component)

        // Assert
        assertTrue(result is Result.Success)
        coVerify { dao.insertComponent(any()) }
        coVerify { dao.insertPendingAction(any()) }
    }
}
