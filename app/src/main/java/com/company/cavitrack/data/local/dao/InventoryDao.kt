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

    @Query("DELETE FROM components")
    suspend fun clearComponents()

    // Customers
    @Query("SELECT * FROM customers")
    fun getCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    // Molds
    @Query("SELECT * FROM molds")
    fun getMolds(): Flow<List<MoldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMolds(molds: List<MoldEntity>)

    @Query("DELETE FROM molds")
    suspend fun clearMolds()

    // History
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC")
    fun getHistoryLogs(): Flow<List<HistoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLogs(logs: List<HistoryLogEntity>)

    @Query("DELETE FROM history_logs")
    suspend fun clearHistoryLogs()

    // Pending Actions
    @Query("SELECT * FROM pending_actions ORDER BY timestamp ASC")
    suspend fun getPendingActions(): List<PendingActionEntity>

    @Insert
    suspend fun insertPendingAction(action: PendingActionEntity)

    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)
}
