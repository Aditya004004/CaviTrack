package com.company.cavitrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.util.DataResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
}
