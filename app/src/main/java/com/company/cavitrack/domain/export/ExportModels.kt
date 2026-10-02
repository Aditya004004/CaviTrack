package com.company.cavitrack.domain.export

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable

@Keep
enum class DataSetType(val displayName: String, val description: String) {
    Inventory("Components & Inventory", "Stock quantities, SKUs, categories, and thresholds"),
    Customers("Customers", "Customer contact info, addresses, notes, and activity"),
    Molds("Molds", "Mold status, codes, cavity counts, and locations"),
    HistoryLogs("History Records", "Audit trails, manual updates, and status change logs"),
    Alerts("Alerts & Thresholds", "Low stock alerts and maintenance required molds"),
    AnalyticsSummary("Analytics Summary", "Valuation metrics, distribution, and key statistics"),
    FullBackup("Full Database Backup", "Complete JSON export of all database collections")
}

@Keep
enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF(".pdf", "application/pdf", "PDF Document (.pdf)"),
    EXCEL(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Spreadsheet (.xlsx)"),
    CSV(".csv", "text/csv", "Comma-Separated Values (.csv)")
}

@Immutable
data class ExportFilterSpec(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val category: String? = null,
    val status: String? = null,
    val location: String? = null,
    val minQty: Int? = null,
    val maxQty: Int? = null,
    val performedBy: String? = null,
    val searchQuery: String? = null
)

@Immutable
data class ExportFilterPreset(
    val id: String,
    val name: String,
    val targetDataSets: Set<DataSetType>,
    val filterSpec: ExportFilterSpec,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
data class ExportMetadataRecord(
    val id: String,
    val fileName: String,
    val format: ExportFormat,
    val dataSets: List<DataSetType>,
    val recordCount: Int,
    val fileSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val performedBy: String = "User",
    val fileUri: String
)

@Keep
enum class ScheduledFrequency(val displayName: String) {
    Daily("Daily (8:00 AM)"),
    Weekly("Weekly (Mon 8:00 AM)"),
    Monthly("Monthly (1st 8:00 AM)")
}

@Immutable
data class ScheduledExportConfig(
    val id: String,
    val title: String,
    val frequency: ScheduledFrequency,
    val format: ExportFormat,
    val dataSets: Set<DataSetType>,
    val recipientEmail: String,
    val enabled: Boolean,
    val lastRunTimestamp: Long? = null
)

@Immutable
data class PreFlightExportSummary(
    val totalRecords: Int = 0,
    val estimatedSizeBytes: Long = 0L,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

sealed interface ExportProgressState {
    data object Idle : ExportProgressState
    data class Processing(val currentStep: String, val progressPercentage: Float) : ExportProgressState
    data class Success(val metadata: ExportMetadataRecord) : ExportProgressState
    data class Error(val message: String) : ExportProgressState
}
