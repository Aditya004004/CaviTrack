package com.company.cavitrack.presentation.inventory.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.navigation.Route
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val useCases: InventoryUseCases
) : ViewModel() {

    private val route: Route.CustomerDetail = savedStateHandle.toRoute()
    val entityId: String = route.id

    private val _uiState = MutableStateFlow<UiState<Customer>>(UiState.Loading)
    val uiState: StateFlow<UiState<Customer>> = _uiState.asStateFlow()

    init {
        loadCustomer()
    }

    fun loadCustomer(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh && _uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }
            try {
                when (val result = useCases.getCustomer(entityId)) {
                    is DataResult.Success -> _uiState.value = UiState.Success(result.data)
                    is DataResult.Error -> {
                        if (_uiState.value !is UiState.Success) {
                            _uiState.value = UiState.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error(e.message ?: "Failed to load customer")
                }
            }
        }
    }

    fun retry() {
        loadCustomer()
    }

    fun deleteCustomer(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = useCases.deleteCustomer(entityId)
                if (result is DataResult.Success) {
                    onSuccess()
                } else if (result is DataResult.Error) {
                    _uiState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = UiState.Error(e.message ?: "Failed to delete customer")
            }
        }
    }
}
