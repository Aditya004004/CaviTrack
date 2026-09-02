package com.company.cavitrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_metrics")

@Singleton
class LocalMetricsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val TOTAL_COMPONENTS = longPreferencesKey("total_components")
        val LOW_STOCK_COUNT = longPreferencesKey("low_stock_count")
        val TOTAL_CUSTOMERS = longPreferencesKey("total_customers")
        val ACTIVE_MOLDS = longPreferencesKey("active_molds")
    }

    val totalComponents: Flow<Long> = context.dataStore.data.map { it[TOTAL_COMPONENTS] ?: 0L }
    val lowStockCount: Flow<Long> = context.dataStore.data.map { it[LOW_STOCK_COUNT] ?: 0L }
    val totalCustomers: Flow<Long> = context.dataStore.data.map { it[TOTAL_CUSTOMERS] ?: 0L }
    val activeMolds: Flow<Long> = context.dataStore.data.map { it[ACTIVE_MOLDS] ?: 0L }

    suspend fun saveMetrics(
        components: Long,
        lowStock: Long,
        customers: Long,
        molds: Long
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_COMPONENTS] = components
            prefs[LOW_STOCK_COUNT] = lowStock
            prefs[TOTAL_CUSTOMERS] = customers
            prefs[ACTIVE_MOLDS] = molds
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
