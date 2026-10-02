package com.company.cavitrack.domain.export

import com.company.cavitrack.data.export.CsvExportEngine
import com.company.cavitrack.data.export.ExcelExportEngine
import com.company.cavitrack.data.export.ExportFileManager
import com.company.cavitrack.data.export.PdfReportExportEngine
import com.company.cavitrack.data.export.StreamingDataFetcher
import com.company.cavitrack.data.repository.ExportHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportEngineManager @Inject constructor(
    private val fetcher: StreamingDataFetcher,
    private val csvEngine: CsvExportEngine,
    private val pdfEngine: PdfReportExportEngine,
    private val excelEngine: ExcelExportEngine,
    private val fileManager: ExportFileManager,
    private val historyRepository: ExportHistoryRepository
) {

    fun executeExport(
        fileName: String,
        dataSets: Set<DataSetType>,
        format: ExportFormat,
        filterSpec: ExportFilterSpec,
        includeCharts: Boolean = true
    ): Flow<ExportProgressState> = flow {
        try {
            emit(ExportProgressState.Processing("Querying database & calculating records...", 0.15f))

            var components = emptyList<com.company.cavitrack.domain.model.Component>()
            var customers = emptyList<com.company.cavitrack.domain.model.Customer>()
            var molds = emptyList<com.company.cavitrack.domain.model.Mold>()
            var historyLogs = emptyList<com.company.cavitrack.domain.model.HistoryLog>()

            if (dataSets.contains(DataSetType.Inventory) || dataSets.contains(DataSetType.Alerts) || dataSets.contains(DataSetType.FullBackup)) {
                components = fetcher.fetchComponents(filterSpec)
            }
            if (dataSets.contains(DataSetType.Customers) || dataSets.contains(DataSetType.FullBackup)) {
                customers = fetcher.fetchCustomers(filterSpec)
            }
            if (dataSets.contains(DataSetType.Molds) || dataSets.contains(DataSetType.FullBackup)) {
                molds = fetcher.fetchMolds(filterSpec)
            }
            if (dataSets.contains(DataSetType.HistoryLogs) || dataSets.contains(DataSetType.FullBackup)) {
                historyLogs = fetcher.fetchHistoryLogs(filterSpec)
            }

            val totalRecordCount = components.size + customers.size + molds.size + historyLogs.size
            emit(ExportProgressState.Processing("Fetched $totalRecordCount items. Building ${format.displayName}...", 0.50f))

            val bytes: ByteArray = when (format) {
                ExportFormat.CSV -> csvEngine.generateCsv(dataSets, components, customers, molds, historyLogs)
                ExportFormat.EXCEL -> excelEngine.generateExcel(dataSets, components, customers, molds, historyLogs)
                ExportFormat.PDF -> pdfEngine.generatePdfReport(dataSets, components, customers, molds, historyLogs, includeCharts)
            }

            emit(ExportProgressState.Processing("Saving report to device storage...", 0.85f))

            val (_, fileUri) = fileManager.saveExportToCache(fileName, format, bytes)

            // Save to public Downloads as well for user convenience
            fileManager.saveExportToDownloads(fileName, format, bytes)

            val record = ExportMetadataRecord(
                id = UUID.randomUUID().toString(),
                fileName = if (fileName.endsWith(format.extension)) fileName else "$fileName${format.extension}",
                format = format,
                dataSets = dataSets.toList(),
                recordCount = totalRecordCount,
                fileSizeBytes = bytes.size.toLong(),
                timestamp = System.currentTimeMillis(),
                performedBy = "Active User",
                fileUri = fileUri.toString()
            )

            historyRepository.addRecord(record)

            emit(ExportProgressState.Success(record))
        } catch (e: Exception) {
            emit(ExportProgressState.Error(e.localizedMessage ?: "Failed to generate export document"))
        }
    }.flowOn(Dispatchers.IO)
}
