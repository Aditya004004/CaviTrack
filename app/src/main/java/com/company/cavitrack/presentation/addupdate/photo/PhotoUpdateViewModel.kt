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
        val log = com.company.cavitrack.domain.model.HistoryLog(
            id = java.util.UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = "Photo"
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
                    val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        val maxDim = 1280f
                        val scale = Math.min(maxDim / bitmap.width, maxDim / bitmap.height)
                        if (scale < 1f) {
                            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                            java.io.FileOutputStream(photoFile).use { out ->
                                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                            }
                        }
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
                _error.value = e.message
            } finally {
                _isUploading.value = false
                if (success && photoFile.exists()) {
                    photoFile.delete()
                }
            }
        }
    }
}
