package com.company.cavitrack.data.repository

import android.util.Log
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.local.entity.PendingActionEntity
import com.company.cavitrack.data.local.entity.toDomain
import com.company.cavitrack.data.remote.dto.*
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.company.cavitrack.data.local.worker.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstInventoryRepository @Inject constructor(
    private val dao: InventoryDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val syncScheduler: com.company.cavitrack.util.SyncScheduler
) : InventoryRepository {

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    override fun getComponents(): Flow<Result<List<Component>>> = dao.getComponents(currentUserId)
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Component>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override suspend fun getComponent(id: String): Result<Component> {
        return try {
            val entity = dao.getComponent(id, currentUserId)
            if (entity != null) Result.Success(entity.toDomain())
            else Result.Error("Not found locally")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.Error(e.message ?: "Database error")
        }
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> = dao.getCustomers(currentUserId)
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Customer>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getMolds(): Flow<Result<List<Mold>>> = dao.getMolds(currentUserId)
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Mold>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getHistory(): Flow<Result<List<HistoryLog>>> = dao.getHistoryLogs(currentUserId)
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<HistoryLog>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override suspend fun saveComponent(component: Component): Result<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return Result.Error("Must be authenticated to save")
            val componentWithOwner = component.copy(ownerId = uid)
            val dto = componentWithOwner.toDto()
            
            // Check if it exists to decide CREATE vs UPDATE
            val exists = dao.getComponent(component.id, uid) != null
            val action = if (exists) "UPDATE" else "CREATE"
            
            dao.insertComponent(dto.toEntity())
            queueAction(action, "COMPONENT", component.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveCustomer(customer: Customer): Result<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return Result.Error("Must be authenticated to save")
            val customerWithOwner = customer.copy(ownerId = uid)
            val dto = customerWithOwner.toDto()
            // Simplified: could do the same exists check if updates were fully supported
            dao.insertCustomers(listOf(dto.toEntity()))
            queueAction("CREATE", "CUSTOMER", customer.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveMold(mold: Mold): Result<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return Result.Error("Must be authenticated to save")
            val moldWithOwner = mold.copy(ownerId = uid)
            val dto = moldWithOwner.toDto()
            dao.insertMolds(listOf(dto.toEntity()))
            queueAction("CREATE", "MOLD", mold.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveHistoryLog(log: HistoryLog): Result<Unit> {
        return try {
            val uid = currentUserId
            if (uid.isBlank()) return Result.Error("Must be authenticated to save")
            val logWithOwner = log.copy(ownerId = uid)
            val dto = logWithOwner.toDto()
            dao.insertHistoryLogs(listOf(dto.toEntity()))
            queueAction("CREATE", "HISTORY", log.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    private suspend fun queueAction(actionType: String, entityType: String, entityId: String, payloadJson: String) {
        dao.insertPendingAction(
            PendingActionEntity(
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                payloadJson = payloadJson
            )
        )
        syncScheduler.scheduleOneTimeSync(androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    override suspend fun refreshData() {
        if (currentUserId.isBlank()) return // Don't fetch if no user

        val compDocs = firestore.collection("components").whereEqualTo("ownerId", currentUserId).get().await()
        val comps = compDocs.toObjects(ComponentDto::class.java)
        dao.refreshComponents(currentUserId, comps.map { it.toEntity() })

        val custDocs = firestore.collection("customers").whereEqualTo("ownerId", currentUserId).get().await()
        val custs = custDocs.toObjects(CustomerDto::class.java)
        dao.refreshCustomers(currentUserId, custs.map { it.toEntity() })

        val moldDocs = firestore.collection("molds").whereEqualTo("ownerId", currentUserId).get().await()
        val molds = moldDocs.toObjects(MoldDto::class.java)
        dao.refreshMolds(currentUserId, molds.map { it.toEntity() })

        val histDocs = firestore.collection("history").whereEqualTo("ownerId", currentUserId).get().await()
        val hists = histDocs.toObjects(HistoryLogDto::class.java)
        dao.refreshHistoryLogs(currentUserId, hists.map { it.toEntity() })
    }

    override suspend fun clearUserData(uid: String) {
        if (uid.isBlank()) return
        
        // 1. Delete from Firestore in batches
        val collections = listOf("components", "customers", "molds", "history")
        for (coll in collections) {
            val docs = firestore.collection(coll).whereEqualTo("ownerId", uid).get().await()
            val chunks = docs.documents.chunked(500)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (doc in chunk) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        }
        
        // 2. Delete from Room (we can use the refresh methods with empty lists to clear them)
        dao.refreshComponents(uid, emptyList())
        dao.refreshCustomers(uid, emptyList())
        dao.refreshMolds(uid, emptyList())
        dao.refreshHistoryLogs(uid, emptyList())
        dao.clearAllPendingActions()
    }

    override suspend fun hasPendingActions(): Boolean {
        return dao.getPendingActionsCount() > 0
    }

    override suspend fun clearAllPendingActions() {
        dao.clearAllPendingActions()
    }
}

