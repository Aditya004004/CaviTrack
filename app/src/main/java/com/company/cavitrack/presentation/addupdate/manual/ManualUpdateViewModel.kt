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
    
    private suspend fun writeHistory(entityType: String, entityId: String, entityName: String, action: String, before: String? = null, after: String? = null, note: String = "") {
        val source = if (note.isNotBlank()) "Manual - $note" else "Manual"
        val log = HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = source,
            beforeValue = before,
            afterValue = after
        )
        repository.saveHistoryLog(log)
    }

    private val _currentQty = MutableStateFlow<Int?>(null)
    val currentQty: StateFlow<Int?> = _currentQty.asStateFlow()

    fun loadComponent(entityId: String) {
        viewModelScope.launch {
            repository.getComponent(entityId).collect { result ->
                if (result is Result.Success) {
                    _currentQty.value = result.data.qty
                }
            }
        }
    }

    fun updateQuantity(
        entityType: String, 
        entityId: String?, 
        newQuantity: Int, 
        note: String, 
        name: String = "", 
        sku: String = "", 
        category: String = "",
        phone: String = "",
        email: String = "",
        address: String = ""
    ) {
        viewModelScope.launch {
            if (entityId != null) {
                if (entityType == "Component") {
                    repository.getComponent(entityId).collect { result ->
                        if (result is Result.Success) {
                            val component = result.data
                            val updated = component.copy(qty = newQuantity, updatedAt = System.currentTimeMillis())
                            val saveResult = repository.saveComponent(updated)
                            if (saveResult is Result.Success) {
                                writeHistory(entityType, component.id, component.name, "Stock Adjusted", component.qty.toString(), newQuantity.toString(), note)
                                _isSaved.value = true
                            } else if (saveResult is Result.Error) {
                                _error.value = saveResult.message
                            }
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                } else {
                    _error.value = "Editing existing $entityType is not supported yet."
                }
            } else {
                if (name.isBlank()) {
                    _error.value = "Name is required."
                    return@launch
                }
                when (entityType) {
                    "Component" -> {
                        if (sku.isBlank()) {
                            _error.value = "SKU is required."
                            return@launch
                        }
                        val component = Component(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            sku = sku,
                            category = category.ifBlank { "General" },
                            qty = newQuantity,
                            unit = "pcs",
                            minStockThreshold = 10
                        )
                        val result = repository.saveComponent(component)
                        if (result is Result.Success) {
                            writeHistory(entityType, component.id, component.name, "Created", null, newQuantity.toString(), note)
                            _isSaved.value = true
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                    "Customer" -> {
                        val customer = com.company.cavitrack.domain.model.Customer(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            phone = phone,
                            email = email,
                            address = address
                        )
                        val result = repository.saveCustomer(customer)
                        if (result is Result.Success) {
                            writeHistory(entityType, customer.id, customer.name, "Created", null, null, note)
                            _isSaved.value = true
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                    "Mold" -> {
                        if (sku.isBlank()) {
                            _error.value = "Mold Code is required."
                            return@launch
                        }
                        val mold = com.company.cavitrack.domain.model.Mold(
                            id = UUID.randomUUID().toString(),
                            moldCode = sku,
                            cavityCount = newQuantity.takeIf { it > 0 } ?: 1,
                            status = com.company.cavitrack.domain.model.MoldStatus.Active,
                            location = category.ifBlank { "Storage" }
                        )
                        val result = repository.saveMold(mold)
                        if (result is Result.Success) {
                            writeHistory(entityType, mold.id, mold.moldCode, "Created", null, null, note)
                            _isSaved.value = true
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                }
            }
        }
    }
}
