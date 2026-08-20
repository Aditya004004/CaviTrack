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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun uploadPhotoAndUpdateEntity(entityType: String, entityId: String, photoFile: File) {
        viewModelScope.launch {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                val fileRef = storageRef.child("photos/\${photoFile.name}")
                val uri = android.net.Uri.fromFile(photoFile)
                
                fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                when (entityType) {
                    "Component" -> {
                        repository.getComponent(entityId).collect { result ->
                            if (result is Result.Success) {
                                val updated = result.data.copy(photoUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                                val saveResult = repository.saveComponent(updated)
                                if (saveResult is Result.Success) {
                                    _isSaved.value = true
                                } else if (saveResult is Result.Error) {
                                    _error.value = saveResult.message
                                }
                            } else if (result is Result.Error) {
                                _error.value = result.message
                            }
                        }
                    }
                    "Customer" -> {
                        // TODO if needed
                        _isSaved.value = true
                    }
                    "Mold" -> {
                        // TODO if needed
                        _isSaved.value = true
                    }
                    else -> _error.value = "Unknown entity type"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
