package com.company.cavitrack.presentation.addupdate.photo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.repository.AuthRepository
import com.company.cavitrack.domain.repository.StorageRepository
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.util.DataResult
import com.company.cavitrack.util.ImageUtil
import com.company.cavitrack.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PhotoUpdateViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _isSaved = Channel<Unit>(capacity = Channel.BUFFERED)
    val isSaved = _isSaved.receiveAsFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<UiText?>(null)
    val error: StateFlow<UiText?> = _error.asStateFlow()

    private suspend fun writeHistory(entityType: EntityType, entityId: String, entityName: String, action: String, photoUrl: String?) {
        val performer = authRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: authRepository.getCurrentUserEmail() ?: "Unknown"
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
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.w("PhotoUpdateViewModel", "Failed to save history log: ${saveResult.message}")
            }
        }
    }

    fun uploadPhotoAndUpdateEntity(entityType: EntityType, entityId: String, photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            try {
                if (entityType != EntityType.Component) {
                    _error.value = UiText.DynamicString("Attaching photos to existing $entityType is not supported yet.")
                    return@launch
                }

                val userId = authRepository.getCurrentUserUid()
                if (userId.isNullOrBlank()) {
                    _error.value = UiText.DynamicString("User not authenticated.")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    ImageUtil.downscaleImage(photoFile)
                }

                val uploadPath = "photos/$userId/${UUID.randomUUID()}.jpg"
                val uploadResult = storageRepository.uploadPhoto(photoFile, uploadPath)
                if (uploadResult is DataResult.Error) {
                    _error.value = UiText.DynamicString(uploadResult.message)
                    return@launch
                }

                val downloadUrl = (uploadResult as DataResult.Success).data

                val result = useCases.getComponent(entityId)
                if (result is DataResult.Success) {
                    val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                    val saveResult = useCases.saveComponent(updated)
                    if (saveResult is DataResult.Success) {
                        writeHistory(entityType, updated.id, updated.name, "Photo Added", downloadUrl)
                        _isSaved.send(Unit)
                    } else if (saveResult is DataResult.Error) {
                        _error.value = UiText.DynamicString(saveResult.message)
                    }
                } else if (result is DataResult.Error) {
                    _error.value = UiText.DynamicString(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = UiText.DynamicString(e.message ?: "Failed to upload photo. Please check your internet connection.")
            } finally {
                _isUploading.value = false
                if (photoFile.exists()) {
                    photoFile.delete()
                }
            }
        }
    }
}
