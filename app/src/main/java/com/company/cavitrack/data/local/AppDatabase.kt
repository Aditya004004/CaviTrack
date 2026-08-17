package com.company.cavitrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.local.entity.*

@Database(
    entities = [
        ComponentEntity::class,
        CustomerEntity::class,
        MoldEntity::class,
        HistoryLogEntity::class,
        PendingActionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val inventoryDao: InventoryDao
}
