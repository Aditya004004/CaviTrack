package com.company.cavitrack.data.local.worker







import android.net.Uri
import java.io.File
import org.json.JSONObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.company.cavitrack.data.local.dao.InventoryDao
import com.company.cavitrack.data.remote.dto.*
import com.google.firebase.firestore.FirebaseFirestoreException
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
    private val repository: com.company.cavitrack.domain.repository.InventoryRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
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
                        "UPLOAD_PHOTO" -> {
                            val payloadObj = JSONObject(action.payloadJson)
                            val entityId = payloadObj.getString("entityId")
                            val filePath = payloadObj.getString("filePath")
                            val file = File(filePath)
                            if (file.exists()) {
                                val user = firebaseAuth.currentUser
                                val uid = user?.uid ?: throw IllegalArgumentException("User not authenticated, cannot upload photo")
                                val storageRef = firebaseStorage.reference
                                val fileRef = storageRef.child("photos/$uid/${file.name}")
                                val uri = Uri.fromFile(file)
                                fileRef.putFile(uri).await()
                                val downloadUrl = fileRef.downloadUrl.await().toString()

                                // Update Firestore
                                firestore.collection("components").document(entityId).update("photoUrl", downloadUrl).await()

                                // Update local Room DB
                                val component = dao.getComponent(entityId, uid)
                                if (component != null) {
                                    val updated = component.copy(photoUrl = downloadUrl)
                                    dao.insertComponent(updated)
                                }
                                file.delete()
                            }
                        }
                        else -> {
                            // Unknown action type, ignore and delete pending action
                        }
                    }
                    dao.deletePendingAction(action)
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE || 
                        e.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED) {
                        allSuccess = false
                    } else {
                        android.util.Log.e("SyncWorker", "Permanent Firestore error syncing action: ${action.id}", e)
                        cleanupPhotoFileIfNeeded(action)
                        dao.deletePendingAction(action) // Permanent error
                    }
                } catch (e: kotlinx.serialization.SerializationException) {
                    android.util.Log.e("SyncWorker", "Serialization error for action: ${action.id}", e)
                    cleanupPhotoFileIfNeeded(action)
                    dao.deletePendingAction(action) // Permanent error (JSON parse)
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("SyncWorker", "Illegal argument error for action: ${action.id}", e)
                    cleanupPhotoFileIfNeeded(action)
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
    private fun cleanupPhotoFileIfNeeded(action: PendingActionEntity) {
        if (action.actionType == "UPLOAD_PHOTO") {
            try {
                val payloadObj = org.json.JSONObject(action.payloadJson)
                if (payloadObj.has("filePath")) {
                    val file = java.io.File(payloadObj.getString("filePath"))
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors during cleanup
            }
        }
    }
}






