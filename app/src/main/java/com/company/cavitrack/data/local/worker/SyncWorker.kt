package com.company.cavitrack.data.local.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.remote.dto.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dao: InventoryDao,
    private val firestore: FirebaseFirestore,
    private val repository: com.company.cavitrack.domain.repository.InventoryRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingActions = dao.getPendingActions()
        if (pendingActions.isNotEmpty()) {
            var allSuccess = true
            for (action in pendingActions) {
                try {
                    when (action.actionType) {
                        "CREATE", "UPDATE", "UPSERT" -> {
                            when (action.entityType) {
                                "COMPONENT" -> {
                                    val dto = Json.decodeFromString<ComponentDto>(action.payloadJson)
                                    firestore.collection("components").document(dto.id).set(dto).await()
                                }
                                "CUSTOMER" -> {
                                    val dto = Json.decodeFromString<CustomerDto>(action.payloadJson)
                                    firestore.collection("customers").document(dto.id).set(dto).await()
                                }
                                "MOLD" -> {
                                    val dto = Json.decodeFromString<MoldDto>(action.payloadJson)
                                    firestore.collection("molds").document(dto.id).set(dto).await()
                                }
                                "HISTORY" -> {
                                    val dto = Json.decodeFromString<HistoryLogDto>(action.payloadJson)
                                    firestore.collection("history").document(dto.id).set(dto).await()
                                }
                            }
                        }
                        "DELETE" -> {
                            when (action.entityType) {
                                "COMPONENT" -> firestore.collection("components").document(action.entityId).delete().await()
                                "CUSTOMER" -> firestore.collection("customers").document(action.entityId).delete().await()
                                "MOLD" -> firestore.collection("molds").document(action.entityId).delete().await()
                                "HISTORY" -> firestore.collection("history").document(action.entityId).delete().await()
                            }
                        }
                        else -> {
                            // Unknown action type, ignore and delete pending action
                        }
                    }
                    dao.deletePendingAction(action)
                } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                    if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE || 
                        e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                        e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        allSuccess = false
                    } else {
                        android.util.Log.e("SyncWorker", "Permanent Firestore error syncing action: ${action.id}", e)
                        dao.deletePendingAction(action) // Permanent error
                    }
                } catch (e: kotlinx.serialization.SerializationException) {
                    android.util.Log.e("SyncWorker", "Serialization error for action: ${action.id}", e)
                    dao.deletePendingAction(action) // Permanent error (JSON parse)
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("SyncWorker", "Illegal argument error for action: ${action.id}", e)
                    dao.deletePendingAction(action) // Permanent error (e.g. empty ID)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    allSuccess = false // Retry on other exceptions
                }
            }
            if (!allSuccess) return Result.retry()
        }
        
        try {
            repository.refreshData()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            return Result.retry()
        }
        
        return Result.success()
    }
}


