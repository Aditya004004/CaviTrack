package com.company.cavitrack.data.local.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.remote.api.CaviTrackApi
import com.company.cavitrack.data.remote.dto.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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
                try {
                    val response = when (action.entityType) {
                        "COMPONENT" -> {
                            val dto = Json.decodeFromString<ComponentDto>(action.payloadJson)
                            if (action.actionType == "CREATE") api.createComponent(dto)
                            else api.updateComponent(action.entityId, dto)
                        }
                        "CUSTOMER" -> {
                            val dto = Json.decodeFromString<CustomerDto>(action.payloadJson)
                            if (action.actionType == "CREATE") api.createCustomer(dto)
                            else api.updateCustomer(action.entityId, dto)
                        }
                        "MOLD" -> {
                            val dto = Json.decodeFromString<MoldDto>(action.payloadJson)
                            if (action.actionType == "CREATE") api.createMold(dto)
                            else api.updateMold(action.entityId, dto)
                        }
                        else -> null
                    }

                    if (response?.isSuccessful == true || response == null) {
                        dao.deletePendingAction(action)
                    } else {
                        allSuccess = false
                    }
                } catch (e: Exception) {
                    allSuccess = false
                }
            }

            if (allSuccess) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
