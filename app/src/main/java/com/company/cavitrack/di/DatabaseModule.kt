package com.company.cavitrack.di

import android.content.Context
import androidx.room.Room
import com.company.cavitrack.data.local.AppDatabase
import com.company.cavitrack.data.local.dao.InventoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE components ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE customers ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE molds ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE history_logs ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE pending_actions ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_components_ownerId_sku` ON `components` (`ownerId`, `sku`)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cavitrack.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         .fallbackToDestructiveMigration(true)
         .build()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(db: AppDatabase): InventoryDao {
        return db.inventoryDao
    }
}
