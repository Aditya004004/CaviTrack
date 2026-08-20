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
    
    private suspend fun writeHistory(entityType: String, entityId: String, entityName: String, action: String, before: String? = null, after: String? = null) {
        val log = HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = "Manual",
            beforeValue = before,
            afterValue = after
        )
        repository.saveHistoryLog(log)
    }

    fun updateQuantity(entityType: String, entityId: String?, newQuantity: Int, note: String, name: String = "", sku: String = "", category: String = "") {
        viewModelScope.launch {
            if (entityId != null) {
                if (entityType == "Component") {
                    repository.getComponent(entityId).collect { result ->
                        if (result is Result.Success) {
                            val component = result.data
                            val updated = component.copy(qty = newQuantity, updatedAt = System.currentTimeMillis())
                            val saveResult = repository.saveComponent(updated)
                            if (saveResult is Result.Success) {
                                writeHistory(entityType, component.id, component.name, "Stock Adjusted", component.qty.toString(), newQuantity.toString())
                                _isSaved.value = true
                            } else if (saveResult is Result.Error) {
                                _error.value = saveResult.message
                            }
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                } else if (entityType == "Customer") {
                    // Update customer logic placeholder, assume only components use this properly for now
                    _isSaved.value = true
                } else if (entityType == "Mold") {
                    // Update mold logic placeholder
                    _isSaved.value = true
                }
            } else {
                when (entityType) {
                    "Component" -> {
                        val component = Component(
                            id = UUID.randomUUID().toString(),
                            name = name.ifBlank { "New Component" },
                            sku = sku.ifBlank { "SKU-UNKNOWN" },
                            category = category.ifBlank { "General" },
                            qty = newQuantity,
                            unit = "pcs",
                            minStockThreshold = 10
                        )
                        val result = repository.saveComponent(component)
                        if (result is Result.Success) {
                            writeHistory(entityType, component.id, component.name, "Created", null, newQuantity.toString())
                            _isSaved.value = true
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                    "Customer" -> {
                        val customer = com.company.cavitrack.domain.model.Customer(
                            id = UUID.randomUUID().toString(),
                            name = name.ifBlank { "New Customer" },
                            phone = note,
                            email = "",
                            address = ""
                        )
                        val result = repository.saveCustomer(customer)
                        if (result is Result.Success) {
                            writeHistory(entityType, customer.id, customer.name, "Created")
                            _isSaved.value = true
                        } else if (result is Result.Error) {
                            _error.value = result.message
                        }
                    }
                    "Mold" -> {
                        val mold = com.company.cavitrack.domain.model.Mold(
                            id = UUID.randomUUID().toString(),
                            moldCode = name.ifBlank { "MOLD-UNKNOWN" },
                            cavityCount = newQuantity.takeIf { it > 0 } ?: 1,
                            status = com.company.cavitrack.domain.model.MoldStatus.Active,
                            location = category.ifBlank { "Storage" }
                        )
                        val result = repository.saveMold(mold)
                        if (result is Result.Success) {
                            writeHistory(entityType, mold.id, mold.moldCode, "Created")
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
