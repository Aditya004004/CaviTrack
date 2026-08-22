package com.company.cavitrack.di

import android.content.Context
import androidx.room.Room
import com.company.cavitrack.data.local.AppDatabase
import com.company.cavitrack.data.local.dao.InventoryDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE components ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE customers ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE molds ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE history_logs ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cavitrack.db"
        ).addMigrations(MIGRATION_1_2)
         .build()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(db: AppDatabase): InventoryDao {
        return db.inventoryDao
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): kotlinx.coroutines.CoroutineScope {
        return kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    }
}

@javax.inject.Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
