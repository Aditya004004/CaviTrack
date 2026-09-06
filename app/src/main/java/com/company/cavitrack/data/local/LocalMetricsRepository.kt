package com.company.cavitrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_metrics")

@Singleton
class LocalMetricsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val TOTAL_COMPONENTS = longPreferencesKey("total_components")
        val LOW_STOCK_COUNT = longPreferencesKey("low_stock_count")
        val TOTAL_CUSTOMERS = longPreferencesKey("total_customers")
        val ACTIVE_MOLDS = longPreferencesKey("active_molds")
        val NOTIFICATION_PROMPT_DISMISSED = booleanPreferencesKey("notification_prompt_dismissed")
    }

    private val safeData: Flow<Preferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val totalComponents: Flow<Long> get() = getTotalComponents("")
    val lowStockCount: Flow<Long> get() = getLowStockCount("")
    val totalCustomers: Flow<Long> get() = getTotalCustomers("")
    val activeMolds: Flow<Long> get() = getActiveMolds("")

    fun getTotalComponents(uid: String = ""): Flow<Long> {
        val key = if (uid.isNotBlank()) longPreferencesKey("${uid}_total_components") else TOTAL_COMPONENTS
        return safeData.map { it[key] ?: 0L }
    }

    fun getLowStockCount(uid: String = ""): Flow<Long> {
        val key = if (uid.isNotBlank()) longPreferencesKey("${uid}_low_stock_count") else LOW_STOCK_COUNT
        return safeData.map { it[key] ?: 0L }
    }

    fun getTotalCustomers(uid: String = ""): Flow<Long> {
        val key = if (uid.isNotBlank()) longPreferencesKey("${uid}_total_customers") else TOTAL_CUSTOMERS
        return safeData.map { it[key] ?: 0L }
    }

    fun getActiveMolds(uid: String = ""): Flow<Long> {
        val key = if (uid.isNotBlank()) longPreferencesKey("${uid}_active_molds") else ACTIVE_MOLDS
        return safeData.map { it[key] ?: 0L }
    }

    suspend fun saveMetrics(
        uid: String = "",
        components: Long,
        lowStock: Long,
        customers: Long,
        molds: Long
    ) {
        context.dataStore.edit { prefs ->
            if (uid.isNotBlank()) {
                prefs[longPreferencesKey("${uid}_total_components")] = components
                prefs[longPreferencesKey("${uid}_low_stock_count")] = lowStock
                prefs[longPreferencesKey("${uid}_total_customers")] = customers
                prefs[longPreferencesKey("${uid}_active_molds")] = molds
            } else {
                prefs[TOTAL_COMPONENTS] = components
                prefs[LOW_STOCK_COUNT] = lowStock
                prefs[TOTAL_CUSTOMERS] = customers
                prefs[ACTIVE_MOLDS] = molds
            }
        }
    }

    val isNotificationPromptDismissed: Flow<Boolean> = safeData.map {
        it[NOTIFICATION_PROMPT_DISMISSED] ?: false
    }

    suspend fun setNotificationPromptDismissed(dismissed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_PROMPT_DISMISSED] = dismissed
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
