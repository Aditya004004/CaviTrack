package com.company.cavitrack.data.export

import com.company.cavitrack.data.remote.dto.ComponentDto
import com.company.cavitrack.data.remote.dto.CustomerDto
import com.company.cavitrack.data.remote.dto.HistoryLogDto
import com.company.cavitrack.data.remote.dto.MoldDto
import com.company.cavitrack.data.remote.dto.toDomain
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportFilterSpec
import com.company.cavitrack.domain.export.PreFlightExportSummary
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamingDataFetcher @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    private val maxBatchLimit = 5000L

    suspend fun getPreFlightSummary(
        dataSets: Set<DataSetType>,
        filterSpec: ExportFilterSpec
    ): PreFlightExportSummary = withContext(Dispatchers.IO) {
        if (dataSets.isEmpty()) return@withContext PreFlightExportSummary()

        var totalCount = 0
        var estimatedBytes = 0L

        if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts) || dataSets.contains(DataSetType.FullBackup)) {
            val components = fetchComponents(filterSpec)
            totalCount += components.size
            estimatedBytes += components.size * 256L
        }

        if (dataSets.contains(DataSetType.Customers) || dataSets.contains(DataSetType.FullBackup)) {
            val customers = fetchCustomers(filterSpec)
            totalCount += customers.size
            estimatedBytes += customers.size * 300L
        }

        if (dataSets.contains(DataSetType.Molds) || dataSets.contains(DataSetType.FullBackup)) {
            val molds = fetchMolds(filterSpec)
            totalCount += molds.size
            estimatedBytes += molds.size * 220L
        }

        if (dataSets.contains(DataSetType.HistoryLogs) || dataSets.contains(DataSetType.FullBackup)) {
            val history = fetchHistoryLogs(filterSpec)
            totalCount += history.size
            estimatedBytes += history.size * 350L
        }

        PreFlightExportSummary(
            totalRecords = totalCount,
            estimatedSizeBytes = estimatedBytes,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    suspend fun fetchComponents(filterSpec: ExportFilterSpec): List<Component> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("components")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            .limit(maxBatchLimit)
            .get()
            .await()

        var items = snapshot.documents.mapNotNull { it.toObject(ComponentDto::class.java)?.toDomain() }

        filterSpec.category?.let { cat ->
            if (cat.isNotBlank()) items = items.filter { it.category.equals(cat, ignoreCase = true) }
        }
        filterSpec.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                val q = query.lowercase()
                items = items.filter { it.name.lowercase().contains(q) || it.sku.lowercase().contains(q) }
            }
        }
        filterSpec.minQty?.let { min ->
            items = items.filter { it.qty >= min }
        }
        filterSpec.maxQty?.let { max ->
            items = items.filter { it.qty <= max }
        }
        filterSpec.startDate?.let { start ->
            items = items.filter { it.createdAt >= start }
        }
        filterSpec.endDate?.let { end ->
            items = items.filter { it.createdAt <= end }
        }

        items.sortedByDescending { it.createdAt }
    }

    suspend fun fetchCustomers(filterSpec: ExportFilterSpec): List<Customer> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("customers")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            .limit(maxBatchLimit)
            .get()
            .await()

        var items = snapshot.documents.mapNotNull { it.toObject(CustomerDto::class.java)?.toDomain() }

        filterSpec.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                val q = query.lowercase()
                items = items.filter { it.name.lowercase().contains(q) || it.email.lowercase().contains(q) || it.phone.contains(q) }
            }
        }
        filterSpec.startDate?.let { start ->
            items = items.filter { it.createdAt >= start }
        }
        filterSpec.endDate?.let { end ->
            items = items.filter { it.createdAt <= end }
        }

        items.sortedByDescending { it.createdAt }
    }

    suspend fun fetchMolds(filterSpec: ExportFilterSpec): List<Mold> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("molds")
            .whereEqualTo("ownerId", currentUserId)
            .whereEqualTo("isDeleted", false)
            .limit(maxBatchLimit)
            .get()
            .await()

        var items = snapshot.documents.mapNotNull { it.toObject(MoldDto::class.java)?.toDomain() }

        filterSpec.status?.let { st ->
            if (st.isNotBlank()) items = items.filter { it.status.name.equals(st, ignoreCase = true) }
        }
        filterSpec.location?.let { loc ->
            if (loc.isNotBlank()) items = items.filter { it.location.equals(loc, ignoreCase = true) }
        }
        filterSpec.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                val q = query.lowercase()
                items = items.filter { it.moldCode.lowercase().contains(q) || it.location.lowercase().contains(q) }
            }
        }
        filterSpec.startDate?.let { start ->
            items = items.filter { it.createdAt >= start }
        }
        filterSpec.endDate?.let { end ->
            items = items.filter { it.createdAt <= end }
        }

        items.sortedByDescending { it.createdAt }
    }

    suspend fun fetchHistoryLogs(filterSpec: ExportFilterSpec): List<HistoryLog> = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("history")
            .whereEqualTo("ownerId", currentUserId)
            .limit(maxBatchLimit)
            .get()
            .await()

        var items = snapshot.documents.mapNotNull { it.toObject(HistoryLogDto::class.java)?.toDomain() }

        filterSpec.performedBy?.let { pb ->
            if (pb.isNotBlank()) items = items.filter { it.performedBy.equals(pb, ignoreCase = true) }
        }
        filterSpec.searchQuery?.let { query ->
            if (query.isNotBlank()) {
                val q = query.lowercase()
                items = items.filter { it.entityName.lowercase().contains(q) || it.action.lowercase().contains(q) }
            }
        }
        filterSpec.startDate?.let { start ->
            items = items.filter { it.timestamp >= start }
        }
        filterSpec.endDate?.let { end ->
            items = items.filter { it.timestamp <= end }
        }

        items.sortedByDescending { it.timestamp }
    }
}
