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
    @param:ApplicationContext private val context: Context
) : InventoryRepository {

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    override fun getComponents(): Flow<Result<List<Component>>> = dao.getComponents(currentUserId)
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Component>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getComponent(id: String): Flow<Result<Component>> = kotlinx.coroutines.flow.flow {
        val entity = dao.getComponent(id, currentUserId)
        if (entity != null) emit(Result.Success(entity.toDomain()))
        else emit(Result.Error("Not found locally"))
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
            val componentWithOwner = component.copy(ownerId = currentUserId)
            val dto = componentWithOwner.toDto()
            dao.insertComponent(dto.toEntity())
            queueAction("CREATE", "COMPONENT", component.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveCustomer(customer: Customer): Result<Unit> {
        return try {
            val customerWithOwner = customer.copy(ownerId = currentUserId)
            val dto = customerWithOwner.toDto()
            dao.insertCustomers(listOf(dto.toEntity()))
            queueAction("CREATE", "CUSTOMER", customer.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveMold(mold: Mold): Result<Unit> {
        return try {
            val moldWithOwner = mold.copy(ownerId = currentUserId)
            val dto = moldWithOwner.toDto()
            dao.insertMolds(listOf(dto.toEntity()))
            queueAction("CREATE", "MOLD", mold.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save locally")
        }
    }

    override suspend fun saveHistoryLog(log: HistoryLog): Result<Unit> {
        return try {
            val logWithOwner = log.copy(ownerId = currentUserId)
            val dto = logWithOwner.toDto()
            dao.insertHistoryLogs(listOf(dto.toEntity()))
            queueAction("CREATE", "HISTORY", log.id, Json.encodeToString(dto))
            Result.Success(Unit)
        } catch (e: Exception) {
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
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueue(syncRequest)
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

    override suspend fun clearUserData() {
        val uid = currentUserId
        if (uid.isBlank()) return
        
        // 1. Delete from Firestore
        val collections = listOf("components", "customers", "molds", "history")
        for (coll in collections) {
            val docs = firestore.collection(coll).whereEqualTo("ownerId", uid).get().await()
            for (doc in docs) {
                firestore.collection(coll).document(doc.id).delete().await()
            }
        }
        
        // 2. Delete from Room (we can use the refresh methods with empty lists to clear them)
        dao.refreshComponents(uid, emptyList())
        dao.refreshCustomers(uid, emptyList())
        dao.refreshMolds(uid, emptyList())
        dao.refreshHistoryLogs(uid, emptyList())
    }
}

