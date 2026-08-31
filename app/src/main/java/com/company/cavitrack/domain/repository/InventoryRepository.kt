package com.company.cavitrack.domain.repository

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.flow.Flow

import androidx.paging.PagingData

interface InventoryRepository {
    fun getComponents(): Flow<PagingData<Component>>
    suspend fun getComponent(id: String): DataResult<Component>
    fun getCustomers(): Flow<PagingData<Customer>>
    suspend fun getCustomer(id: String): DataResult<Customer>
    fun getMolds(): Flow<PagingData<Mold>>
    suspend fun getMold(id: String): DataResult<Mold>
    fun getHistory(): Flow<DataResult<List<HistoryLog>>>
    fun getRecentHistory(limit: Int): Flow<DataResult<List<HistoryLog>>>

    suspend fun getComponentsCount(): DataResult<Long>
    suspend fun getLowStockComponentsCount(): DataResult<Long>
    suspend fun getCustomersCount(): DataResult<Long>
    suspend fun getActiveMoldsCount(): DataResult<Long>

    suspend fun saveComponent(component: Component): DataResult<Unit>
    suspend fun saveCustomer(customer: Customer): DataResult<Unit>
    suspend fun saveMold(mold: Mold): DataResult<Unit>
    suspend fun saveHistoryLog(log: HistoryLog): DataResult<Unit>

    suspend fun deleteComponent(id: String): DataResult<Unit>
    suspend fun deleteCustomer(id: String): DataResult<Unit>
    suspend fun deleteMold(id: String): DataResult<Unit>

    suspend fun clearUserData(): DataResult<Unit>
}
