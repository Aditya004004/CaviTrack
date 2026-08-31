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

    private val _isSaved = kotlinx.coroutines.channels.Channel<Unit>()
    val isSaved = _isSaved.receiveAsFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private suspend fun writeHistory(entityType: EntityType, entityId: String, entityName: String, action: String, before: String? = null, after: String? = null, note: String = "") {
        val performer = authRepository.getCurrentUserName()?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserEmail() ?: "Unknown"
        val log = HistoryLog(
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
        val saveResult = useCases.saveHistoryLog(log)
        if (saveResult is DataResult.Error) {
            if (com.company.cavitrack.BuildConfig.DEBUG) android.util.Log.e("History", "Failed to save history log: ${saveResult.message}")
        }
    }

    private val _currentQty = MutableStateFlow<Int?>(null)
    val currentQty: StateFlow<Int?> = _currentQty.asStateFlow()

    fun loadComponent(entityId: String) {
        viewModelScope.launch {
            try {
                val result = useCases.getComponent(entityId)
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
                when (val result = useCases.getComponent(entityId)) {
                    is DataResult.Success -> {
                        val component = result.data
                        val updated = component.copy(qty = newQuantity, updatedAt = System.currentTimeMillis())
                        when (val saveResult = useCases.saveComponent(updated)) {
                            is DataResult.Success -> {
                                writeHistory(EntityType.Component, component.id, component.name, "Stock Adjusted", component.qty.toString(), newQuantity.toString(), note)
                                _isSaved.send(Unit)
                            }
                            is DataResult.Error -> _error.value = saveResult.message
                        }
                    }
                    is DataResult.Error -> _error.value = result.message
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message ?: "Failed to update component"
            } finally {
                _isSaving.value = false
            }
        }
    }

    private inline fun executeWithLoading(crossinline action: suspend () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                action()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message ?: "Operation failed"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createComponent(name: String, sku: String, category: String, initialQuantity: Int, note: String) {
        executeWithLoading {
            if (name.isBlank()) { _error.value = "Name is required."; return@executeWithLoading }
            if (sku.isBlank()) { _error.value = "SKU is required."; return@executeWithLoading }
            val component = Component(
                id = UUID.randomUUID().toString(), name = name, sku = sku,
                category = category.ifBlank { "General" }, qty = initialQuantity,
                unit = "pcs", minStockThreshold = 10,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val result = useCases.saveComponent(component)
            if (result is DataResult.Success) {
                writeHistory(EntityType.Component, component.id, component.name, "Created", null, initialQuantity.toString(), note)
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = result.message
            }
        }
    }

    fun createCustomer(name: String, phone: String, email: String, address: String, note: String) {
        executeWithLoading {
            if (name.isBlank()) { _error.value = "Name is required."; return@executeWithLoading }
            val customer = Customer(
                id = UUID.randomUUID().toString(), name = name, phone = phone, email = email, address = address,
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
            val result = useCases.saveCustomer(customer)
            if (result is DataResult.Success) {
                writeHistory(EntityType.Customer, customer.id, customer.name, "Created", null, null, note)
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = result.message
            }
        }
    }

    fun createMold(moldCode: String, cavityCount: Int, location: String, note: String) {
        executeWithLoading {
            if (moldCode.isBlank()) { _error.value = "Mold Code is required."; return@executeWithLoading }
            val mold = Mold(
                id = UUID.randomUUID().toString(), moldCode = moldCode,
                cavityCount = cavityCount.takeIf { it > 0 } ?: 1,
                status = MoldStatus.Active,
                location = location.ifBlank { "Storage" },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val result = useCases.saveMold(mold)
            if (result is DataResult.Success) {
                writeHistory(EntityType.Mold, mold.id, mold.moldCode, "Created", null, null, note)
                _isSaved.send(Unit)
            } else if (result is DataResult.Error) {
                _error.value = result.message
            }
        }
    } // closes fun
} // closes class







