package com.company.cavitrack.presentation.addupdate.photo








import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.company.cavitrack.util.DataResult

@HiltViewModel
class PhotoUpdateViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private suspend fun writeHistory(entityType: com.company.cavitrack.domain.model.EntityType, entityId: String, entityName: String, action: String, photoUrl: String?) {
        val user = firebaseAuth.currentUser
        val performer = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Unknown"
        val log = com.company.cavitrack.domain.model.HistoryLog(
            id = java.util.UUID.randomUUID().toString(),
            entityType = entityType.name,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = "Photo",
            photoUrl = photoUrl,
            performedBy = performer
        )
        val saveResult = repository.saveHistoryLog(log)
        if (saveResult is DataResult.Error) {
            android.util.Log.e("History", "Failed to save history log: ${saveResult.message}")
        }
    }

    fun uploadPhotoAndUpdateEntity(entityType: com.company.cavitrack.domain.model.EntityType, entityId: String, photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            var success = false
            try {
                val user = firebaseAuth.currentUser
                val uid = user?.uid ?: "unknown"
                val storageRef = firebaseStorage.reference
                val fileRef = storageRef.child("photos/$uid/${photoFile.name}")
                
                // Downscale image before upload
                withContext(Dispatchers.Default) {
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath, options)
                    
                    val maxDim = 1280
                    var inSampleSize = 1
                    if (options.outHeight > maxDim || options.outWidth > maxDim) {
                        val halfHeight = options.outHeight / 2
                        val halfWidth = options.outWidth / 2
                        while ((halfHeight / inSampleSize) >= maxDim || (halfWidth / inSampleSize) >= maxDim) {
                            inSampleSize *= 2
                        }
                    }
                    
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }
                    try {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath, decodeOptions)
                        if (bitmap != null) {
                            try {
                                java.io.FileOutputStream(photoFile).use { out ->
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                                }
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    } catch (t: Throwable) {
                        // Handle OOM or decode errors gracefully
                        android.util.Log.e("PhotoUpdateViewModel", "Error processing photo", t)
                    }
                }
                
                val uri = Uri.fromFile(photoFile)
                
                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                if (entityType == com.company.cavitrack.domain.model.EntityType.Component) {
                    // It's a suspend method now!
                    val result = repository.getComponent(entityId)
                    if (result is DataResult.Success) {
                        val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                        val saveResult = repository.saveComponent(updated)
                        if (saveResult is DataResult.Success) {
                            writeHistory(entityType, updated.id, updated.name, "Photo Added", downloadUrl)
                            success = true
                            _isSaved.value = true
                        } else if (saveResult is DataResult.Error) {
                            _error.value = saveResult.message
                        }
                    } else if (result is DataResult.Error) {
                        _error.value = result.message
                    }
                } else {
                    _error.value = "Attaching photos to existing $entityType is not supported yet."
                }
            } catch (e: Exception) {
                val isCancellation = e is kotlinx.coroutines.CancellationException
                
                if (entityType == com.company.cavitrack.domain.model.EntityType.Component) {
                    // Even if cancelled mid-upload, queue it for background sync
                    kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                        repository.queuePhotoUpload(entityId, photoFile.absolutePath)
                        
                        val result = repository.getComponent(entityId)
                        if (result is DataResult.Success) {
                            val photoUri = "file://${photoFile.absolutePath}"
                            val updated = result.data.copy(photoUrl = photoUri, updatedAt = System.currentTimeMillis())
                            repository.saveComponent(updated)
                            writeHistory(entityType, updated.id, updated.name, "Photo Added (Offline Pending)", photoUri)
                        }
                    }
                    success = true
                    _isSaved.value = true
                    if (!isCancellation) {
                        _error.value = "Photo queued for upload when online."
                    }
                } else {
                    if (!isCancellation) {
                        _error.value = e.message
                    }
                }
                
                if (isCancellation) throw e
            } finally {
                _isUploading.value = false
                if (!success && photoFile.exists()) {
                    photoFile.delete()
                }
            }
        }
    }
}







