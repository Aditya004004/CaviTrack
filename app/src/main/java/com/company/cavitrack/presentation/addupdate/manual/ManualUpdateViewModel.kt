package com.company.cavitrack.presentation.addupdate.manual



import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.company.cavitrack.util.DataResult
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.HistoryLog
import java.util.UUID

@HiltViewModel
class ManualUpdateViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private suspend fun writeHistory(entityType: String, entityId: String, entityName: String, action: String, before: String? = null, after: String? = null, note: String = "") {
        val source = if (note.isNotBlank()) "Manual - $note" else "Manual"
        val user = firebaseAuth.currentUser
        val performer = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Unknown"
        val log = HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = source,
            beforeValue = before,
            afterValue = after,
            performedBy = performer
        )
        val saveResult = repository.saveHistoryLog(log)
        if (saveResult is DataResult.Error) {
            android.util.Log.e("History", "Failed to save history log: ${saveResult.message}")
        }
    }

    private val _currentQty = MutableStateFlow<Int?>(null)
    val currentQty: StateFlow<Int?> = _currentQty.asStateFlow()

    fun loadComponent(entityId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getComponent(entityId)
                if (result is DataResult.Success) {
                    _currentQty.value = result.data.qty
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message ?: "Failed to load component"
            }
        }
    }

    fun updateComponentQuantity(entityId: String, newQuantity: Int, note: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val result = repository.getComponent(entityId)
                if (result is DataResult.Success) {
                    val component = result.data
                    val updated = component.copy(qty = newQuantity, updatedAt = System.currentTimeMillis())
                    val saveResult = repository.saveComponent(updated)
                    if (saveResult is DataResult.Success) {
                        writeHistory(com.company.cavitrack.domain.model.EntityType.Component.name, component.id, component.name, "Stock Adjusted", component.qty.toString(), newQuantity.toString(), note)
                        _isSaved.value = true
                    } else if (saveResult is DataResult.Error) {
                        _error.value = saveResult.message
                    }
                } else if (result is DataResult.Error) {
                    _error.value = result.message
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message ?: "Failed to update component"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createComponent(name: String, sku: String, category: String, initialQuantity: Int, note: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                if (name.isBlank()) { _error.value = "Name is required."; return@launch }
                if (sku.isBlank()) { _error.value = "SKU is required."; return@launch }
                val component = Component(
                    id = UUID.randomUUID().toString(), name = name, sku = sku,
                    category = category.ifBlank { "General" }, qty = initialQuantity,
                    unit = "pcs", minStockThreshold = 10
                )
                val result = repository.saveComponent(component)
                if (result is DataResult.Success) {
                    writeHistory(com.company.cavitrack.domain.model.EntityType.Component.name, component.id, component.name, "Created", null, initialQuantity.toString(), note)
                    _isSaved.value = true
                } else if (result is DataResult.Error) {
                    _error.value = result.message
                }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createCustomer(name: String, phone: String, email: String, address: String, note: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                if (name.isBlank()) { _error.value = "Name is required."; return@launch }
                val customer = com.company.cavitrack.domain.model.Customer(
                    id = UUID.randomUUID().toString(), name = name, phone = phone, email = email, address = address
                )
                val result = repository.saveCustomer(customer)
                if (result is DataResult.Success) {
                    writeHistory(com.company.cavitrack.domain.model.EntityType.Customer.name, customer.id, customer.name, "Created", null, null, note)
                    _isSaved.value = true
                } else if (result is DataResult.Error) {
                    _error.value = result.message
                }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createMold(moldCode: String, cavityCount: Int, location: String, note: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                if (moldCode.isBlank()) { _error.value = "Mold Code is required."; return@launch }
                val mold = com.company.cavitrack.domain.model.Mold(
                    id = UUID.randomUUID().toString(), moldCode = moldCode,
                    cavityCount = cavityCount.takeIf { it > 0 } ?: 1,
                    status = com.company.cavitrack.domain.model.MoldStatus.Active,
                    location = location.ifBlank { "Storage" }
                )
                val result = repository.saveMold(mold)
                if (result is DataResult.Success) {
                    writeHistory(com.company.cavitrack.domain.model.EntityType.Mold.name, mold.id, mold.moldCode, "Created", null, null, note)
                    _isSaved.value = true
                } else if (result is DataResult.Error) {
                    _error.value = result.message
                }
            } finally {
                _isSaving.value = false
            }
        }
    } // closes fun
} // closes class







