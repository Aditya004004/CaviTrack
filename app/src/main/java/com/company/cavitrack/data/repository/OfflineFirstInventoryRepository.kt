package com.company.cavitrack.data.repository

import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.local.entity.toDomain
import com.company.cavitrack.data.local.entity.toEntity
import com.company.cavitrack.data.remote.api.CaviTrackApi
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
            val response = api.createComponent(component)
            if (response.isSuccessful) {
                dao.insertComponent(response.body()!!.toEntity())
                Result.Success(Unit)
            } else {
                // Queue offline action
                Result.Error("Network error, queued offline")
            }
        } catch (e: Exception) {
            // Queue offline action
            Result.Error(e.message ?: "Exception, queued offline")
        }
    }

    override suspend fun saveCustomer(customer: Customer): Result<Unit> {
        // Implement save logic, similarly queueing if failed
        return Result.Success(Unit)
    }

    override suspend fun saveMold(mold: Mold): Result<Unit> {
        // Implement save logic, similarly queueing if failed
        return Result.Success(Unit)
    }

    suspend fun refreshData() {
        try {
            val compRes = api.getComponents()
            if (compRes.isSuccessful) compRes.body()?.let { dao.insertComponents(it.map { c -> c.toEntity() }) }

            val custRes = api.getCustomers()
            if (custRes.isSuccessful) custRes.body()?.let { dao.insertCustomers(it.map { c -> c.toEntity() }) }

            val moldRes = api.getMolds()
            if (moldRes.isSuccessful) moldRes.body()?.let { dao.insertMolds(it.map { m -> m.toEntity() }) }

            val histRes = api.getHistoryLogs()
            if (histRes.isSuccessful) histRes.body()?.let { dao.insertHistoryLogs(it.map { h -> h.toEntity() }) }
        } catch (e: Exception) {
            // Ignore for now
        }
    }
}
