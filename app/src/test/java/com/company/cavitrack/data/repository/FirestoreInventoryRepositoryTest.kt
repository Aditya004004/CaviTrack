package com.company.cavitrack.data.repository

import com.company.cavitrack.domain.model.ChangeSource
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.model.MoldStatus
import com.company.cavitrack.util.DataResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreInventoryRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: FirestoreInventoryRepository

    @Before
    fun setup() {
        firestore = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)

        repository = FirestoreInventoryRepository(
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    @Test
    fun `saveComponent returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val component = Component(id = "1", name = "Test", sku = "SKU1", category = "Cat", qty = 5, minStockThreshold = 10, unit = "pcs", ownerId = "", createdAt = 1L, updatedAt = 1L)

        // Act
        val result = repository.saveComponent(component)

        // Assert
        assertTrue(result is DataResult.Error)
        assertTrue((result as DataResult.Error).message.contains("Not authenticated"))
    }

    @Test
    fun `updateComponentQuantityTransaction returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val log = HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = EntityType.Component,
            entityId = "comp-1",
            entityName = "Widget",
            action = "Stock Adjusted",
            changeSource = ChangeSource.Manual,
            performedBy = "Tester",
            timestamp = System.currentTimeMillis()
        )

        // Act
        val result = repository.updateComponentQuantityTransaction("comp-1", 15, log)

        // Assert
        assertTrue(result is DataResult.Error)
        assertEquals("Not authenticated", (result as DataResult.Error).message)
    }

    @Test
    fun `saveComponentWithHistory returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val component = Component(id = "1", name = "Test", sku = "SKU1", category = "Cat", qty = 5, minStockThreshold = 10, unit = "pcs", ownerId = "", createdAt = 1L, updatedAt = 1L)
        val log = HistoryLog(id = "log-1", entityType = EntityType.Component, entityId = "1", entityName = "Test", action = "Created", changeSource = ChangeSource.Manual, performedBy = "Tester", timestamp = 1L)

        // Act
        val result = repository.saveComponentWithHistory(component, log)

        // Assert
        assertTrue(result is DataResult.Error)
        assertEquals("Not authenticated", (result as DataResult.Error).message)
    }

    @Test
    fun `saveCustomerWithHistory returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val customer = Customer(id = "cust-1", name = "Acme Corp", phone = "123", email = "a@b.com", address = "123 St", createdAt = 1L, updatedAt = 1L)
        val log = HistoryLog(id = "log-2", entityType = EntityType.Customer, entityId = "cust-1", entityName = "Acme Corp", action = "Created", changeSource = ChangeSource.Manual, performedBy = "Tester", timestamp = 1L)

        // Act
        val result = repository.saveCustomerWithHistory(customer, log)

        // Assert
        assertTrue(result is DataResult.Error)
        assertEquals("Not authenticated", (result as DataResult.Error).message)
    }

    @Test
    fun `saveMoldWithHistory returns Error when user is not authenticated`() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null
        val mold = Mold(id = "mold-1", moldCode = "M-101", cavityCount = 4, status = MoldStatus.Active, location = "Storage", createdAt = 1L, updatedAt = 1L)
        val log = HistoryLog(id = "log-3", entityType = EntityType.Mold, entityId = "mold-1", entityName = "M-101", action = "Created", changeSource = ChangeSource.Manual, performedBy = "Tester", timestamp = 1L)

        // Act
        val result = repository.saveMoldWithHistory(mold, log)

        // Assert
        assertTrue(result is DataResult.Error)
        assertEquals("Not authenticated", (result as DataResult.Error).message)
    }
}
