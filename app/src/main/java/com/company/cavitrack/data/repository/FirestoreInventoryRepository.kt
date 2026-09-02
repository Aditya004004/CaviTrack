package com.company.cavitrack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import androidx.paging.PagingData
import com.company.cavitrack.data.remote.dto.ComponentDto
import com.company.cavitrack.data.remote.dto.CustomerDto
import com.company.cavitrack.data.remote.dto.HistoryLogDto
import com.company.cavitrack.data.remote.dto.MoldDto
import com.company.cavitrack.data.remote.dto.toDomain
import com.company.cavitrack.data.remote.dto.toDto
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreInventoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : InventoryRepository {

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    // ──────────────────────────────────────────────
    // Flow-based real-time listeners
    // ──────────────────────────────────────────────

    override fun getComponents(searchQuery: String, lowStockOnly: Boolean): Flow<PagingData<Component>> {
        var query = firestore.collection("components")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            
        if (lowStockOnly) {
            query = query.whereEqualTo("isLowStock", true)
        }
        
        if (searchQuery.isNotBlank()) {
            // Note: Multiple inequalities / orderBy must follow Firestore rules.
            // When querying by name (orderBy), we cannot easily combine with other fields unless indexed.
            // Simple approach for prefix search:
            query = query.orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
        }

        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = 20)
        ) {
            com.company.cavitrack.data.paging.FirestorePagingSource(query) { doc -> 
                doc.toObject(ComponentDto::class.java)?.toDomain() 
            }
        }.flow
    }

    override fun getCustomers(searchQuery: String): Flow<PagingData<Customer>> {
        var query = firestore.collection("customers")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            
        if (searchQuery.isNotBlank()) {
            query = query.orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
        }

        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = 20)
        ) {
            com.company.cavitrack.data.paging.FirestorePagingSource(query) { doc -> 
                doc.toObject(CustomerDto::class.java)?.toDomain() 
            }
        }.flow
    }

    override fun getMolds(searchQuery: String, status: String?): Flow<PagingData<Mold>> {
        var query = firestore.collection("molds")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            
        if (status != null) {
            query = query.whereEqualTo("status", status)
        }
        
        if (searchQuery.isNotBlank()) {
            query = query.orderBy("moldCode").startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        } else {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
        }

        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = 20)
        ) {
            com.company.cavitrack.data.paging.FirestorePagingSource(query) { doc -> 
                doc.toObject(MoldDto::class.java)?.toDomain() 
            }
        }.flow
    }

    override fun getHistory(action: String?): Flow<PagingData<HistoryLog>> {
        var query = firestore.collection("history")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            
        if (action != null) {
            // Note: This requires a composite index in Firestore!
            // (ownerId ASC, isDeleted ASC, action ASC, timestamp DESC)
            query = query.whereEqualTo("action", action)
        }
        
        query = query.orderBy("timestamp", Query.Direction.DESCENDING)

        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = 20)
        ) {
            com.company.cavitrack.data.paging.FirestorePagingSource(query) { doc ->
                doc.toObject(HistoryLogDto::class.java)?.toDomain()
            }
        }.flow
    }
    override fun getRecentHistory(limit: Int): Flow<DataResult<List<HistoryLog>>> = callbackFlow {
        if (currentUserId.isBlank()) {
            trySend(DataResult.Error("Not authenticated"))
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("history")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataResult.Error(error.message ?: "Firestore error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val logs = snapshot.toObjects(HistoryLogDto::class.java).map { it.toDomain() }
                    trySend(DataResult.Success(logs))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getComponentsCount(): DataResult<Long> {
        if (currentUserId.isBlank()) return DataResult.Error("Not authenticated")
        return try {
            val snapshot = firestore.collection("components")
                .whereEqualTo("ownerId", currentUserId)
                .whereEqualTo("isDeleted", false)
                .count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            DataResult.Success(snapshot.count)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Error fetching count")
        }
    }

    override suspend fun getLowStockComponentsCount(): DataResult<Long> {
        if (currentUserId.isBlank()) return DataResult.Error("Not authenticated")
        return try {
            val snapshot = firestore.collection("components")
                .whereEqualTo("ownerId", currentUserId)
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("isLowStock", true)
                .count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            DataResult.Success(snapshot.count)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Error fetching count")
        }
    }

    override suspend fun getCustomersCount(): DataResult<Long> {
        if (currentUserId.isBlank()) return DataResult.Error("Not authenticated")
        return try {
            val snapshot = firestore.collection("customers")
                .whereEqualTo("ownerId", currentUserId)
                .whereEqualTo("isDeleted", false)
                .count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            DataResult.Success(snapshot.count)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Error fetching count")
        }
    }

    override suspend fun getActiveMoldsCount(): DataResult<Long> {
        if (currentUserId.isBlank()) return DataResult.Error("Not authenticated")
        return try {
            val snapshot = firestore.collection("molds")
                .whereEqualTo("ownerId", currentUserId)
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("status", "Active")
                .count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
            DataResult.Success(snapshot.count)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Error fetching count")
        }
    }

    // ──────────────────────────────────────────────
    // Single-item queries with ownership verification
    // ──────────────────────────────────────────────

    override suspend fun getComponent(id: String): DataResult<Component> {
        return try {
            val doc = firestore.collection("components").document(id).get().await()
            val dto = doc.toObject(ComponentDto::class.java)
            if (dto != null && !dto.isDeleted && dto.ownerId == currentUserId) {
                DataResult.Success(dto.toDomain())
            } else {
                DataResult.Error("Not found")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Database error")
        }
    }

    override suspend fun getCustomer(id: String): DataResult<Customer> {
        return try {
            val doc = firestore.collection("customers").document(id).get().await()
            val dto = doc.toObject(CustomerDto::class.java)
            if (dto != null && !dto.isDeleted && dto.ownerId == currentUserId) {
                DataResult.Success(dto.toDomain())
            } else {
                DataResult.Error("Not found")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Database error")
        }
    }

    override suspend fun getMold(id: String): DataResult<Mold> {
        return try {
            val doc = firestore.collection("molds").document(id).get().await()
            val dto = doc.toObject(MoldDto::class.java)
            if (dto != null && !dto.isDeleted && dto.ownerId == currentUserId) {
                DataResult.Success(dto.toDomain())
            } else {
                DataResult.Error("Not found")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Database error")
        }
    }

    // ──────────────────────────────────────────────
    // Write operations
    // ──────────────────────────────────────────────

    override suspend fun saveComponent(component: Component): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to save")
            val dto = component.copy(ownerId = uid).toDto()
            firestore.collection("components").document(dto.id).set(dto).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to save")
        }
    }

    override suspend fun saveCustomer(customer: Customer): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to save")
            val dto = customer.copy(ownerId = uid).toDto()
            firestore.collection("customers").document(dto.id).set(dto).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to save")
        }
    }

    override suspend fun saveMold(mold: Mold): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to save")
            val dto = mold.copy(ownerId = uid).toDto()
            firestore.collection("molds").document(dto.id).set(dto).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to save")
        }
    }

    override suspend fun saveHistoryLog(log: HistoryLog): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to save")
            val dto = log.copy(ownerId = uid).toDto()
            firestore.collection("history").document(dto.id).set(dto).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to save")
        }
    }

    override suspend fun updateComponentQuantityTransaction(id: String, newQty: Int): DataResult<Component> {
        val uid = currentUserId
        if (uid.isBlank()) return DataResult.Error("Must be authenticated to update")
        val docRef = firestore.collection("components").document(id)
        
        return try {
            val updatedDto = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val dto = snapshot.toObject(ComponentDto::class.java)
                    ?: throw com.google.firebase.firestore.FirebaseFirestoreException("Component not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND)
                    
                if (dto.isDeleted || dto.ownerId != uid) {
                    throw com.google.firebase.firestore.FirebaseFirestoreException("Permission denied", com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED)
                }
                
                val isLowStock = newQty <= dto.minStockThreshold
                val newDto = dto.copy(qty = newQty, isLowStock = isLowStock, updatedAt = System.currentTimeMillis())
                transaction.set(docRef, newDto)
                newDto
            }.await()
            
            DataResult.Success(updatedDto.toDomain())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Transaction failed")
        }
    }

    override suspend fun deleteComponent(id: String): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to delete")
            val updateData = mapOf("isDeleted" to true, "updatedAt" to System.currentTimeMillis())
            firestore.collection("components").document(id).update(updateData).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete")
        }
    }

    override suspend fun deleteCustomer(id: String): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to delete")
            val updateData = mapOf("isDeleted" to true, "updatedAt" to System.currentTimeMillis())
            firestore.collection("customers").document(id).update(updateData).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete")
        }
    }

    override suspend fun deleteMold(id: String): DataResult<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return DataResult.Error("Must be authenticated to delete")
            val updateData = mapOf("isDeleted" to true, "updatedAt" to System.currentTimeMillis())
            firestore.collection("molds").document(id).update(updateData).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete")
        }
    }

    // ──────────────────────────────────────────────
    // Data cleanup (resolves UID internally)
    // ──────────────────────────────────────────────

    override suspend fun clearUserData(): DataResult<Unit> {
        val uid = currentUserId
        if (uid.isBlank()) return DataResult.Error("Not authenticated")

        return try {
            val collections = listOf("components", "customers", "molds", "history")
            for (coll in collections) {
                var query = firestore.collection(coll)
                    .whereEqualTo("ownerId", uid)
                    .limit(500)
                var docs = query.get().await()
                while (docs.documents.isNotEmpty()) {
                    val batch = firestore.batch()
                    for (doc in docs.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                    docs = query.get().await()
                }
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to clear user data")
        }
    }
}
