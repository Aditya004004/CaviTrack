package com.company.cavitrack.presentation.addupdate.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.repository.InventoryRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import com.company.cavitrack.util.Result

@HiltViewModel
class PhotoUpdateViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private suspend fun writeHistory(entityType: String, entityId: String, entityName: String, action: String) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val performer = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Unknown"
        val log = com.company.cavitrack.domain.model.HistoryLog(
            id = java.util.UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = "Photo",
            performedBy = performer
        )
        repository.saveHistoryLog(log)
    }

    fun uploadPhotoAndUpdateEntity(entityType: String, entityId: String, photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            var success = false
            try {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val uid = user?.uid ?: "unknown"
                val storageRef = FirebaseStorage.getInstance().reference
                val fileRef = storageRef.child("photos/$uid/${photoFile.name}")
                
                // Downscale image before upload
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath, options)
                    
                    val maxDim = 1280
                    var inSampleSize = 1
                    if (options.outHeight > maxDim || options.outWidth > maxDim) {
                        val halfHeight = options.outHeight / 2
                        val halfWidth = options.outWidth / 2
                        while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                            inSampleSize *= 2
                        }
                    }
                    
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }
                    try {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath, decodeOptions)
                        if (bitmap != null) {
                            java.io.FileOutputStream(photoFile).use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                            }
                            bitmap.recycle()
                        }
                    } catch (t: Throwable) {
                        // Handle OOM or decode errors gracefully
                    }
                }
                
                val uri = android.net.Uri.fromFile(photoFile)
                
                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                if (entityType == "Component") {
                    // It's a suspend method now!
                    val result = repository.getComponent(entityId)
                    if (result is Result.Success) {
                        val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                        val saveResult = repository.saveComponent(updated)
                        if (saveResult is Result.Success) {
                            writeHistory(entityType, updated.id, updated.name, "Photo Added")
                            success = true
                            _isSaved.value = true
                        } else if (saveResult is Result.Error) {
                            _error.value = saveResult.message
                        }
                    } else if (result is Result.Error) {
                        _error.value = result.message
                    }
                } else {
                    _error.value = "Attaching photos to existing $entityType is not supported yet."
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message
            } finally {
                _isUploading.value = false
                if (photoFile.exists()) {
                    photoFile.delete()
                }
            }
        }
    }
}
