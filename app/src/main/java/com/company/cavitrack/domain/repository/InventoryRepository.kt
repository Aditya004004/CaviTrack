package com.company.cavitrack.domain.repository

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.util.Result
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getComponents(): Flow<Result<List<Component>>>
    suspend fun getComponent(id: String): Result<Component>
    fun getCustomers(): Flow<Result<List<Customer>>>
    fun getMolds(): Flow<Result<List<Mold>>>
    fun getHistory(): Flow<Result<List<HistoryLog>>>
    
    suspend fun saveComponent(component: Component): Result<Unit>
    suspend fun saveCustomer(customer: Customer): Result<Unit>
    suspend fun saveMold(mold: Mold): Result<Unit>
    suspend fun saveHistoryLog(log: HistoryLog): Result<Unit>
    
    suspend fun refreshData()
    suspend fun clearUserData(uid: String)
    suspend fun hasPendingActions(): Boolean
    suspend fun clearAllPendingActions()
}
