package com.company.cavitrack.data.repository

import android.util.Log
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.local.entity.PendingActionEntity
import com.company.cavitrack.data.local.entity.toDomain
import com.company.cavitrack.data.remote.api.CaviTrackApi
import com.company.cavitrack.data.remote.dto.toDto
import com.company.cavitrack.data.remote.dto.toEntity
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstInventoryRepository @Inject constructor(
    private val dao: InventoryDao,
    private val api: CaviTrackApi
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
            val response = api.createComponent(dto)
            if (response.isSuccessful) {
                response.body()?.let { 
                    dao.insertComponent(it.toEntity()) 
                } ?: return Result.Error("Empty response body")
                Result.Success(Unit)
            } else {
                queueAction("CREATE", "COMPONENT", component.id, Json.encodeToString(dto))
                dao.insertComponent(dto.toEntity())
                Result.Error("Network error, queued offline")
            }
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
            val response = api.createCustomer(dto)
            if (response.isSuccessful) {
                response.body()?.let { 
                    dao.insertCustomers(listOf(it.toEntity())) 
                } ?: return Result.Error("Empty response body")
                Result.Success(Unit)
            } else {
                queueAction("CREATE", "CUSTOMER", customer.id, Json.encodeToString(dto))
                dao.insertCustomers(listOf(dto.toEntity()))
                Result.Error("Network error, queued offline")
            }
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
            val response = api.createMold(dto)
            if (response.isSuccessful) {
                response.body()?.let { 
                    dao.insertMolds(listOf(it.toEntity())) 
                } ?: return Result.Error("Empty response body")
                Result.Success(Unit)
            } else {
                queueAction("CREATE", "MOLD", mold.id, Json.encodeToString(dto))
                dao.insertMolds(listOf(dto.toEntity()))
                Result.Error("Network error, queued offline")
            }
        } catch (e: Exception) {
            val dto = mold.toDto()
            queueAction("CREATE", "MOLD", mold.id, Json.encodeToString(dto))
            dao.insertMolds(listOf(dto.toEntity()))
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
    }

    suspend fun refreshData() {
        try {
            val compRes = api.getComponents()
            if (compRes.isSuccessful) compRes.body()?.let { dao.refreshComponents(it.map { c -> c.toEntity() }) }

            val custRes = api.getCustomers()
            if (custRes.isSuccessful) custRes.body()?.let { dao.refreshCustomers(it.map { c -> c.toEntity() }) }

            val moldRes = api.getMolds()
            if (moldRes.isSuccessful) moldRes.body()?.let { dao.refreshMolds(it.map { m -> m.toEntity() }) }

            val histRes = api.getHistoryLogs()
            if (histRes.isSuccessful) histRes.body()?.let { dao.refreshHistoryLogs(it.map { h -> h.toEntity() }) }
        } catch (e: Exception) {
            Log.e("OfflineFirstRepository", "refreshData failed", e)
        }
    }
}
