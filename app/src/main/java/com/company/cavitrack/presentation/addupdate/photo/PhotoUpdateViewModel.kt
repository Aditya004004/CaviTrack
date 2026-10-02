package com.company.cavitrack.presentation.addupdate.photo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.ChangeSource
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.repository.AuthRepository
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.domain.repository.StorageRepository
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

import kotlinx.coroutines.CoroutineDispatcher

@HiltViewModel
class PhotoUpdateViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _isSaved = Channel<Unit>(capacity = Channel.BUFFERED)
    val isSaved = _isSaved.receiveAsFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<UiText?>(null)
    val error: StateFlow<UiText?> = _error.asStateFlow()

    private fun makeHistoryLog(entityType: EntityType, entityId: String, entityName: String, photoUrl: String): HistoryLog {
        val performer = authRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: authRepository.getCurrentUserEmail() ?: "Unknown"
        return HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = "Photo Added",
            changeSource = ChangeSource.Photo,
            photoUrl = photoUrl,
            performedBy = performer,
            timestamp = System.currentTimeMillis()
        )
    }

    fun uploadPhotoAndUpdateEntity(entityType: EntityType, entityId: String, photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            var uploadedDownloadUrl: String? = null
            try {
                val userId = authRepository.getCurrentUserUid()
                if (userId.isNullOrBlank()) {
                    _error.value = UiText.DynamicString("User not authenticated.")
                    return@launch
                }

                withContext(ioDispatcher) {
                    ImageUtil.downscaleImage(photoFile)
                }

                val uploadPath = "photos/$userId/${UUID.randomUUID()}.jpg"
                val uploadResult = storageRepository.uploadPhoto(photoFile, uploadPath)
                if (uploadResult is DataResult.Error) {
                    _error.value = UiText.DynamicString(uploadResult.message)
                    return@launch
                }

                val downloadUrl = (uploadResult as DataResult.Success).data
                uploadedDownloadUrl = downloadUrl
                val now = System.currentTimeMillis()

                var oldPhotoUrl: String? = null
                val saveResult: DataResult<Unit> = when (entityType) {
                    EntityType.Component -> {
                        when (val result = repository.getComponent(entityId)) {
                            is DataResult.Success -> {
                                oldPhotoUrl = result.data.photoUrl
                                val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = now)
                                val log = makeHistoryLog(entityType, updated.id, updated.name, downloadUrl)
                                repository.saveComponentWithHistory(updated, log)
                            }
                            is DataResult.Error -> DataResult.Error(result.message)
                        }
                    }
                    EntityType.Customer -> {
                        when (val result = repository.getCustomer(entityId)) {
                            is DataResult.Success -> {
                                oldPhotoUrl = result.data.photoUrl
                                val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = now)
                                val log = makeHistoryLog(entityType, updated.id, updated.name, downloadUrl)
                                repository.saveCustomerWithHistory(updated, log)
                            }
                            is DataResult.Error -> DataResult.Error(result.message)
                        }
                    }
                    EntityType.Mold -> {
                        when (val result = repository.getMold(entityId)) {
                            is DataResult.Success -> {
                                oldPhotoUrl = result.data.photoUrl
                                val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = now)
                                val log = makeHistoryLog(entityType, updated.id, updated.moldCode, downloadUrl)
                                repository.saveMoldWithHistory(updated, log)
                            }
                            is DataResult.Error -> DataResult.Error(result.message)
                        }
                    }
                    EntityType.History -> {
                        DataResult.Error("Attaching photos to history entries is not supported.")
                    }
                }

                if (saveResult is DataResult.Success) {
                    if (!oldPhotoUrl.isNullOrBlank()) {
                        try {
                            storageRepository.deletePhoto(oldPhotoUrl)
                        } catch (_: Exception) {}
                    }
                    _isSaved.send(Unit)
                } else if (saveResult is DataResult.Error) {
                    try {
                        storageRepository.deletePhoto(downloadUrl)
                    } catch (_: Exception) {}
                    _error.value = UiText.DynamicString(saveResult.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                uploadedDownloadUrl?.let { url ->
                    try {
                        storageRepository.deletePhoto(url)
                    } catch (_: Exception) {}
                }
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
