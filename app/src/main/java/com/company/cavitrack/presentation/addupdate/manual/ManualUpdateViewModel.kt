package com.company.cavitrack.presentation.addupdate.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.company.cavitrack.util.Result
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.HistoryLog
import java.util.UUID

@HiltViewModel
class ManualUpdateViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun updateQuantity(entityType: String, entityId: String?, newQuantity: Int, note: String) {
        viewModelScope.launch {
            if (entityId != null && entityType == "Component") {
                repository.getComponent(entityId).collect { result ->
                    if (result is Result.Success) {
                        val component = result.data
                        val updated = component.copy(qty = newQuantity, updatedAt = System.currentTimeMillis())
                        repository.saveComponent(updated)
                        _isSaved.value = true
                    } else if (result is Result.Error) {
                        _error.value = result.message
                    }
                }
            } else {
                _error.value = "Creating new items is not supported yet."
            }
        }
    }
}
