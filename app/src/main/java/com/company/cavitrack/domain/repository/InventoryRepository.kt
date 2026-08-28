package com.company.cavitrack.domain.repository

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getComponents(): Flow<DataResult<List<Component>>>
    suspend fun getComponent(id: String): DataResult<Component>
    fun getCustomers(): Flow<DataResult<List<Customer>>>
    fun getMolds(): Flow<DataResult<List<Mold>>>
    fun getHistory(): Flow<DataResult<List<HistoryLog>>>
    fun getRecentHistory(limit: Int): Flow<DataResult<List<HistoryLog>>>
    
    suspend fun saveComponent(component: Component): DataResult<Unit>
    suspend fun saveCustomer(customer: Customer): DataResult<Unit>
    suspend fun saveMold(mold: Mold): DataResult<Unit>
    suspend fun saveHistoryLog(log: HistoryLog): DataResult<Unit>
    
    suspend fun deleteComponent(id: String): DataResult<Unit>
    suspend fun deleteCustomer(id: String): DataResult<Unit>
    suspend fun deleteMold(id: String): DataResult<Unit>
    
    suspend fun refreshData()
    suspend fun clearUserData(uid: String)
    suspend fun hasPendingActions(): Boolean
    suspend fun clearAllPendingActions()
    suspend fun queuePhotoUpload(entityId: String, photoFilePath: String)
}

