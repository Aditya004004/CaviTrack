package com.company.cavitrack.presentation.addupdate.manual



import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.company.cavitrack.util.DataResult
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.model.MoldStatus
import java.util.UUID

@HiltViewModel
class ManualUpdateViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    private val authRepository: com.company.cavitrack.domain.repository.AuthRepository
) : ViewModel() {

    private val _isSaved = kotlinx.coroutines.channels.Channel<Unit>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val isSaved = _isSaved.receiveAsFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<com.company.cavitrack.util.UiText?>(null)
    val error: StateFlow<com.company.cavitrack.util.UiText?> = _error.asStateFlow()
    
    private suspend fun makeHistoryLog(
        entityType: EntityType,
        entityId: String,
        entityName: String,
        action: String,
        before: String? = null,
        after: String? = null,
        note: String = ""
    ): HistoryLog {
        val performer = authRepository.getCurrentUserName()?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserEmail() ?: "Unknown"
        return HistoryLog(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            action = action,
            changeSource = com.company.cavitrack.domain.model.ChangeSource.Manual,
            changeNote = note.takeIf { it.isNotBlank() },
            beforeValue = before,
            afterValue = after,
            performedBy = performer,
            timestamp = System.currentTimeMillis()
        )
    }

    private val _currentQty = MutableStateFlow<Int?>(null)
    val currentQty: StateFlow<Int?> = _currentQty.asStateFlow()

    private val _loadedComponent = MutableStateFlow<Component?>(null)
    val loadedComponent: StateFlow<Component?> = _loadedComponent.asStateFlow()

    private val _loadedCustomer = MutableStateFlow<Customer?>(null)
    val loadedCustomer: StateFlow<Customer?> = _loadedCustomer.asStateFlow()

    private val _loadedMold = MutableStateFlow<Mold?>(null)
    val loadedMold: StateFlow<Mold?> = _loadedMold.asStateFlow()

    fun loadComponent(entityId: String) {
        viewModelScope.launch {
            try {
                val result = useCases.getComponent(entityId)
                if (result is DataResult.Success) {
                    _loadedComponent.value = result.data
                    _currentQty.value = result.data.qty
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Failed to load component")
            }
        }
    }

    fun loadCustomer(entityId: String) {
        viewModelScope.launch {
            try {
                val result = useCases.getCustomer(entityId)
                if (result is DataResult.Success) {
                    _loadedCustomer.value = result.data
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Failed to load customer")
            }
        }
    }

    fun loadMold(entityId: String) {
        viewModelScope.launch {
            try {
                val result = useCases.getMold(entityId)
                if (result is DataResult.Success) {
                    _loadedMold.value = result.data
                    _currentQty.value = result.data.cavityCount
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Failed to load mold")
            }
        }
    }

    fun updateComponentQuantity(entityId: String, newQuantity: Int, note: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val componentName = _loadedComponent.value?.name ?: "Component"
                val log = makeHistoryLog(
                    EntityType.Component,
                    entityId,
                    componentName,
                    "Stock Adjusted",
                    _currentQty.value?.toString(),
                    newQuantity.toString(),
                    note
                )
                when (val saveResult = useCases.updateComponentQuantityTransaction(entityId, newQuantity, log)) {
                    is DataResult.Success -> {
                        _isSaved.send(Unit)
                    }
                    is DataResult.Error -> _error.value = com.company.cavitrack.util.UiText.DynamicString(saveResult.message)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Failed to update component")
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun executeWithLoading(action: suspend () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                action()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = com.company.cavitrack.util.UiText.DynamicString(e.message ?: "Operation failed")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createComponent(name: String, sku: String, category: String, initialQuantity: Int, note: String) {
        executeWithLoading {
            if (name.isBlank()) { _error.value = com.company.cavitrack.util.UiText.StringResource(com.company.cavitrack.R.string.error_name_empty); return@executeWithLoading }
            if (sku.isBlank()) { _error.value = com.company.cavitrack.util.UiText.DynamicString("SKU is required."); return@executeWithLoading }
            val component = Component(
                id = UUID.randomUUID().toString(), name = name, sku = sku,
                category = category.ifBlank { "General" }, qty = initialQuantity,
                unit = "pcs", minStockThreshold = 10,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val log = makeHistoryLog(EntityType.Component, component.id, component.name, "Created", null, initialQuantity.toString(), note)
            val result = useCases.saveComponentWithHistory(component, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    }

    fun createCustomer(name: String, phone: String, email: String, address: String, note: String) {
        executeWithLoading {
            if (name.isBlank()) { _error.value = com.company.cavitrack.util.UiText.StringResource(com.company.cavitrack.R.string.error_name_empty); return@executeWithLoading }
            val customer = Customer(
                id = UUID.randomUUID().toString(), name = name, phone = phone, email = email, address = address,
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
            val log = makeHistoryLog(EntityType.Customer, customer.id, customer.name, "Created", null, null, note)
            val result = useCases.saveCustomerWithHistory(customer, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    }

    fun createMold(moldCode: String, cavityCount: Int, location: String, status: MoldStatus = MoldStatus.Active, note: String) {
        executeWithLoading {
            if (moldCode.isBlank()) { _error.value = com.company.cavitrack.util.UiText.DynamicString("Mold Code is required."); return@executeWithLoading }
            if (cavityCount <= 0) { _error.value = com.company.cavitrack.util.UiText.DynamicString("Cavity count must be greater than 0."); return@executeWithLoading }
            val mold = Mold(
                id = UUID.randomUUID().toString(), moldCode = moldCode,
                cavityCount = cavityCount,
                status = status,
                location = location.ifBlank { "Storage" },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val log = makeHistoryLog(EntityType.Mold, mold.id, mold.moldCode, "Created", null, null, note)
            val result = useCases.saveMoldWithHistory(mold, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    }

    fun updateComponent(
        entityId: String,
        name: String,
        sku: String,
        category: String,
        newQuantity: Int,
        note: String
    ) {
        executeWithLoading {
            if (name.isBlank()) {
                _error.value = com.company.cavitrack.util.UiText.StringResource(com.company.cavitrack.R.string.error_name_empty)
                return@executeWithLoading
            }
            if (sku.isBlank()) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("SKU is required.")
                return@executeWithLoading
            }
            if (newQuantity < 0) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Quantity cannot be negative.")
                return@executeWithLoading
            }
            val existing = _loadedComponent.value ?: run {
                val res = useCases.getComponent(entityId)
                if (res is DataResult.Success) res.data else null
            } ?: run {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Unable to retrieve component for update. Please refresh.")
                return@executeWithLoading
            }

            val updated = existing.copy(
                name = name.trim(),
                sku = sku.trim(),
                category = category.trim().ifBlank { "General" },
                qty = newQuantity,
                updatedAt = System.currentTimeMillis()
            )

            val isQtyChanged = existing.qty != newQuantity
            val actionName = if (isQtyChanged) "Stock Adjusted" else "Updated"
            val diffNote = if (isQtyChanged) "Qty: ${existing.qty} -> $newQuantity" else "Metadata updated"
            val combinedNote = if (note.isNotBlank()) "$note ($diffNote)" else diffNote

            val log = makeHistoryLog(
                EntityType.Component,
                entityId,
                updated.name,
                actionName,
                existing.qty.toString(),
                newQuantity.toString(),
                combinedNote
            )

            val result = useCases.saveComponentWithHistory(updated, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    }

    fun updateCustomer(entityId: String, name: String, phone: String, email: String, address: String, note: String) {
        executeWithLoading {
            if (name.isBlank()) {
                _error.value = com.company.cavitrack.util.UiText.StringResource(com.company.cavitrack.R.string.error_name_empty)
                return@executeWithLoading
            }
            val existing = _loadedCustomer.value ?: run {
                val res = useCases.getCustomer(entityId)
                if (res is DataResult.Success) res.data else null
            } ?: run {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Unable to retrieve customer for update. Please refresh.")
                return@executeWithLoading
            }
            val updated = existing.copy(
                name = name,
                phone = phone,
                email = email,
                address = address,
                notes = note.ifBlank { existing.notes },
                updatedAt = System.currentTimeMillis()
            )
            val log = makeHistoryLog(EntityType.Customer, entityId, name, "Updated", existing.name, name, note)
            val result = useCases.saveCustomerWithHistory(updated, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    }

    fun updateMold(entityId: String, moldCode: String, cavityCount: Int, location: String, status: MoldStatus = MoldStatus.Active, note: String) {
        executeWithLoading {
            if (moldCode.isBlank()) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Mold Code is required.")
                return@executeWithLoading
            }
            if (cavityCount <= 0) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Cavity count must be greater than 0.")
                return@executeWithLoading
            }
            val existing = _loadedMold.value ?: run {
                val res = useCases.getMold(entityId)
                if (res is DataResult.Success) res.data else null
            } ?: run {
                _error.value = com.company.cavitrack.util.UiText.DynamicString("Unable to retrieve mold for update. Please refresh.")
                return@executeWithLoading
            }
            val updated = existing.copy(
                moldCode = moldCode,
                cavityCount = cavityCount,
                status = status,
                location = location.ifBlank { existing.location },
                updatedAt = System.currentTimeMillis()
            )
            val statusDiff = if (existing.status != status) " [Status: ${existing.status.name} -> ${status.name}]" else ""
            val log = makeHistoryLog(
                EntityType.Mold,
                entityId,
                moldCode,
                "Updated",
                existing.cavityCount.toString(),
                "$cavityCount$statusDiff",
                note
            )
            val result = useCases.saveMoldWithHistory(updated, log)
            if (result is DataResult.Success) {
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = com.company.cavitrack.util.UiText.DynamicString(result.message)
            }
        }
    } // closes fun
} // closes class







