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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstInventoryRepository @Inject constructor(
    private val dao: InventoryDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : InventoryRepository {

    override fun getComponents(): Flow<Result<List<Component>>> = dao.getComponents()
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Component>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getComponent(id: String): Flow<Result<Component>> = kotlinx.coroutines.flow.flow {
        val entity = dao.getComponent(id)
        if (entity != null) emit(Result.Success(entity.toDomain()))
        else emit(Result.Error("Not found locally"))
    }

    override fun getCustomers(): Flow<Result<List<Customer>>> = dao.getCustomers()
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Customer>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getMolds(): Flow<Result<List<Mold>>> = dao.getMolds()
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<Mold>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override fun getHistory(): Flow<Result<List<HistoryLog>>> = dao.getHistoryLogs()
        .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<HistoryLog>> }
        .catch { emit(Result.Error(it.message ?: "Database error")) }

    override suspend fun saveComponent(component: Component): Result<Unit> {
        return try {
            val dto = component.toDto()
            firestore.collection("components").document(dto.id).set(dto).await()
            dao.insertComponent(dto.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            val dto = component.toDto()
            queueAction("CREATE", "COMPONENT", component.id, Json.encodeToString(dto))
            dao.insertComponent(dto.toEntity())
            Result.Error(e.message ?: "Exception, queued offline")
        }
    }

    override suspend fun saveCustomer(customer: Customer): Result<Unit> {
        return try {
            val dto = customer.toDto()
            firestore.collection("customers").document(dto.id).set(dto).await()
            dao.insertCustomers(listOf(dto.toEntity()))
            Result.Success(Unit)
        } catch (e: Exception) {
            val dto = customer.toDto()
            queueAction("CREATE", "CUSTOMER", customer.id, Json.encodeToString(dto))
            dao.insertCustomers(listOf(dto.toEntity()))
            Result.Error(e.message ?: "Exception, queued offline")
        }
    }

    override suspend fun saveMold(mold: Mold): Result<Unit> {
        return try {
            val dto = mold.toDto()
            firestore.collection("molds").document(dto.id).set(dto).await()
            dao.insertMolds(listOf(dto.toEntity()))
            Result.Success(Unit)
        } catch (e: Exception) {
            val dto = mold.toDto()
            queueAction("CREATE", "MOLD", mold.id, Json.encodeToString(dto))
            dao.insertMolds(listOf(dto.toEntity()))
            Result.Error(e.message ?: "Exception, queued offline")
        }
    }

    override suspend fun saveHistoryLog(log: HistoryLog): Result<Unit> {
        return try {
            val dto = log.toDto()
            firestore.collection("history").document(dto.id).set(dto).await()
            dao.insertHistoryLogs(listOf(dto.toEntity()))
            Result.Success(Unit)
        } catch (e: Exception) {
            val dto = log.toDto()
            queueAction("CREATE", "HISTORY", log.id, Json.encodeToString(dto))
            dao.insertHistoryLogs(listOf(dto.toEntity()))
            Result.Error(e.message ?: "Exception, queued offline")
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
        try {
            val compDocs = firestore.collection("components").get().await()
            val comps = compDocs.toObjects(ComponentDto::class.java)
            dao.refreshComponents(comps.map { it.toEntity() })

            val custDocs = firestore.collection("customers").get().await()
            val custs = custDocs.toObjects(CustomerDto::class.java)
            dao.refreshCustomers(custs.map { it.toEntity() })

            val moldDocs = firestore.collection("molds").get().await()
            val molds = moldDocs.toObjects(MoldDto::class.java)
            dao.refreshMolds(molds.map { it.toEntity() })

            val histDocs = firestore.collection("history").get().await()
            val hists = histDocs.toObjects(HistoryLogDto::class.java)
            dao.refreshHistoryLogs(hists.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e("OfflineFirstRepository", "refreshData failed", e)
        }
    }
}

