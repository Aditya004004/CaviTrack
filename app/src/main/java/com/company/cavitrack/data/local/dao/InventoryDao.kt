package com.company.cavitrack.data.local.dao

import androidx.room.*
import com.company.cavitrack.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    // Components
    @Query("SELECT * FROM components WHERE ownerId = :ownerId")
    fun getComponents(ownerId: String): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE id = :id AND ownerId = :ownerId")
    suspend fun getComponent(id: String, ownerId: String): ComponentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<ComponentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: ComponentEntity)

    @Query("DELETE FROM components WHERE ownerId = :ownerId AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'COMPONENT')")
    suspend fun clearComponents(ownerId: String)

    // Customers
    @Query("SELECT * FROM customers WHERE ownerId = :ownerId")
    fun getCustomers(ownerId: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers WHERE ownerId = :ownerId AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'CUSTOMER')")
    suspend fun clearCustomers(ownerId: String)

    // Molds
    @Query("SELECT * FROM molds WHERE ownerId = :ownerId")
    fun getMolds(ownerId: String): Flow<List<MoldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMolds(molds: List<MoldEntity>)

    @Query("DELETE FROM molds WHERE ownerId = :ownerId AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'MOLD')")
    suspend fun clearMolds(ownerId: String)

    // History
    @Query("SELECT * FROM history_logs WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getHistoryLogs(ownerId: String): Flow<List<HistoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLogs(logs: List<HistoryLogEntity>)

    @Query("DELETE FROM history_logs WHERE ownerId = :ownerId AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'HISTORY')")
    suspend fun clearHistoryLogs(ownerId: String)

    // Pending Actions
    @Query("SELECT * FROM pending_actions ORDER BY timestamp ASC")
    suspend fun getPendingActions(): List<PendingActionEntity>

    @Query("SELECT COUNT(*) FROM pending_actions")
    suspend fun getPendingActionsCount(): Int

    @Insert
    suspend fun insertPendingAction(action: PendingActionEntity)

    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)

    @Query("DELETE FROM components WHERE ownerId = :ownerId AND id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'COMPONENT')")
    suspend fun deleteComponentsNotIn(ownerId: String, ids: List<String>)

    @Query("DELETE FROM customers WHERE ownerId = :ownerId AND id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'CUSTOMER')")
    suspend fun deleteCustomersNotIn(ownerId: String, ids: List<String>)

    @Query("DELETE FROM molds WHERE ownerId = :ownerId AND id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'MOLD')")
    suspend fun deleteMoldsNotIn(ownerId: String, ids: List<String>)

    @Query("DELETE FROM history_logs WHERE ownerId = :ownerId AND id NOT IN (:ids) AND id NOT IN (SELECT entityId FROM pending_actions WHERE entityType = 'HISTORY')")
    suspend fun deleteHistoryLogsNotIn(ownerId: String, ids: List<String>)

    @Transaction
    suspend fun refreshComponents(ownerId: String, components: List<ComponentEntity>) {
        if (components.isNotEmpty()) deleteComponentsNotIn(ownerId, components.map { it.id })
        else clearComponents(ownerId)
        insertComponents(components)
    }

    @Transaction
    suspend fun refreshCustomers(ownerId: String, customers: List<CustomerEntity>) {
        if (customers.isNotEmpty()) deleteCustomersNotIn(ownerId, customers.map { it.id })
        else clearCustomers(ownerId)
        insertCustomers(customers)
    }

    @Transaction
    suspend fun refreshMolds(ownerId: String, molds: List<MoldEntity>) {
        if (molds.isNotEmpty()) deleteMoldsNotIn(ownerId, molds.map { it.id })
        else clearMolds(ownerId)
        insertMolds(molds)
    }

    @Transaction
    suspend fun refreshHistoryLogs(ownerId: String, logs: List<HistoryLogEntity>) {
        if (logs.isNotEmpty()) deleteHistoryLogsNotIn(ownerId, logs.map { it.id })
        else clearHistoryLogs(ownerId)
        insertHistoryLogs(logs)
    }
}

