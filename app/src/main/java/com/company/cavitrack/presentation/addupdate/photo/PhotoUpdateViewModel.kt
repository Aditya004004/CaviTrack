package com.company.cavitrack.presentation.addupdate.photo








import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import com.company.cavitrack.domain.repository.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.company.cavitrack.util.DataResult

import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.util.ImageUtil
import java.util.UUID

@HiltViewModel
class PhotoUpdateViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSaved = kotlinx.coroutines.channels.Channel<Unit>()
    val isSaved = _isSaved.receiveAsFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<com.company.cavitrack.util.UiText?>(null)
    val error: StateFlow<com.company.cavitrack.util.UiText?> = _error.asStateFlow()

    private suspend fun writeHistory(entityType: EntityType, entityId: String, entityName: String, action: String, photoUrl: String?) {
        val performer = authRepository.getCurrentUserName()?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserEmail() ?: "Unknown"
        val log = HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = com.company.cavitrack.domain.model.ChangeSource.Photo,
            photoUrl = photoUrl,
            performedBy = performer,
            timestamp = System.currentTimeMillis()
        )
        val saveResult = useCases.saveHistoryLog(log)
        if (saveResult is DataResult.Error) {
            // Silently ignore history log failures
        }
    }

    fun uploadPhotoAndUpdateEntity(entityType: EntityType, entityId: String, photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            var success = false
            try {
                if (entityType != EntityType.Component) {
                    _error.value = com.company.cavitrack.util.UiText.DynamicString("Attaching photos to existing $entityType is not supported yet.")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    ImageUtil.downscaleImage(photoFile)
                }
                
                val storageRef = FirebaseStorage.getInstance().reference
                val fileRef = storageRef.child("photos/${UUID.randomUUID()}.jpg")
                fileRef.putFile(Uri.fromFile(photoFile)).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                val result = useCases.getComponent(entityId)
                if (result is DataResult.Success) {
                    val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                    val saveResult = useCases.saveComponent(updated)
                    if (saveResult is DataResult.Success) {
                        writeHistory(entityType, updated.id, updated.name, "Photo Added", downloadUrl)
                        success = true
                        _isSaved.send(Unit)
                    } else if (saveResult is DataResult.Error) {
                        _error.value = com.company.cavitrack.util.UiText.DynamicString(saveResult.message)
                    }
                } else if (result is DataResult.Error) {
                    _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
                }
            } catch (e: Exception) {
                val isCancellation = e is kotlinx.coroutines.CancellationException
                
                if (entityType == EntityType.Component) {
                    if (!isCancellation) {
                        _error.value = com.company.cavitrack.util.UiText.DynamicString("Failed to upload photo. Please check your internet connection.")
                    }
                } else {
                    if (!isCancellation) {
                        _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Error")
                    }
                }
                
                if (isCancellation) throw e
            } finally {
                _isUploading.value = false
                if (photoFile.exists()) {
                    photoFile.delete()
                }
            }
        }
    }
}







