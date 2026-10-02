package com.company.cavitrack.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportEngineManager
import com.company.cavitrack.domain.export.ExportFilterSpec
import com.company.cavitrack.domain.export.ExportFormat
import com.company.cavitrack.domain.export.ExportProgressState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.last

@HiltWorker
class ScheduledExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val exportEngineManager: ExportEngineManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val fileName = "Scheduled_Export_${System.currentTimeMillis()}"
            val resultState = exportEngineManager.executeExport(
                fileName = fileName,
                dataSets = setOf(DataSetType.Inventory, DataSetType.Molds),
                format = ExportFormat.PDF,
                filterSpec = ExportFilterSpec()
            ).last()

            if (resultState is ExportProgressState.Success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
