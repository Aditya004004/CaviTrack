package com.company.cavitrack.data.local.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.remote.api.CaviTrackApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: InventoryDao,
    private val api: CaviTrackApi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingActions = dao.getPendingActions()
            if (pendingActions.isEmpty()) {
                return@withContext Result.success()
            }

            var allSuccess = true
            for (action in pendingActions) {
                // In a real implementation, you'd parse `payloadJson` and execute the 
                // correct API call based on `actionType` and `entityType`.
                // e.g., if (action.entityType == "COMPONENT" && action.actionType == "CREATE") { api.createComponent(...) }
                
                // If success:
                dao.deletePendingAction(action)
                // If failure (e.g. 500 error):
                // allSuccess = false
            }

            if (allSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
