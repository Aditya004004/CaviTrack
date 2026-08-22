package com.company.cavitrack.data.local.dao

import androidx.room.*
import com.company.cavitrack.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    // Components
    @Query("SELECT * FROM components")
    fun getComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getComponent(id: String): ComponentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<ComponentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: ComponentEntity)

    @Query("DELETE FROM components WHERE id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'COMPONENT')")
    suspend fun clearComponents()

    // Customers
    @Query("SELECT * FROM customers")
    fun getCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers WHERE id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'CUSTOMER')")
    suspend fun clearCustomers()

    // Molds
    @Query("SELECT * FROM molds")
    fun getMolds(): Flow<List<MoldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMolds(molds: List<MoldEntity>)

    @Query("DELETE FROM molds WHERE id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'MOLD')")
    suspend fun clearMolds()

    // History
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC")
    fun getHistoryLogs(): Flow<List<HistoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLogs(logs: List<HistoryLogEntity>)

    @Query("DELETE FROM history_logs WHERE id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'HISTORY')")
    suspend fun clearHistoryLogs()

    // Pending Actions
    @Query("SELECT * FROM pending_actions ORDER BY timestamp ASC")
    suspend fun getPendingActions(): List<PendingActionEntity>

    @Insert
    suspend fun insertPendingAction(action: PendingActionEntity)

    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)

    @Query("DELETE FROM components WHERE id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'COMPONENT')")
    suspend fun deleteComponentsNotIn(ids: List<String>)

    @Query("DELETE FROM customers WHERE id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'CUSTOMER')")
    suspend fun deleteCustomersNotIn(ids: List<String>)

    @Query("DELETE FROM molds WHERE id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'MOLD')")
    suspend fun deleteMoldsNotIn(ids: List<String>)

    @Query("DELETE FROM history_logs WHERE id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'HISTORY')")
    suspend fun deleteHistoryLogsNotIn(ids: List<String>)

    @Transaction
    suspend fun refreshComponents(components: List<ComponentEntity>) {
        if (components.isNotEmpty()) deleteComponentsNotIn(components.map { it.id })
        else clearComponents()
        insertComponents(components)
    }

    @Transaction
    suspend fun refreshCustomers(customers: List<CustomerEntity>) {
        if (customers.isNotEmpty()) deleteCustomersNotIn(customers.map { it.id })
        else clearCustomers()
        insertCustomers(customers)
    }

    @Transaction
    suspend fun refreshMolds(molds: List<MoldEntity>) {
        if (molds.isNotEmpty()) deleteMoldsNotIn(molds.map { it.id })
        else clearMolds()
        insertMolds(molds)
    }

    @Transaction
    suspend fun refreshHistoryLogs(logs: List<HistoryLogEntity>) {
        if (logs.isNotEmpty()) deleteHistoryLogsNotIn(logs.map { it.id })
        else clearHistoryLogs()
        insertHistoryLogs(logs)
    }
}

