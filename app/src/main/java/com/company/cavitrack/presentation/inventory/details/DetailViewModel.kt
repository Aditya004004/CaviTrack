package com.company.cavitrack.presentation.inventory.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val useCases: InventoryUseCases
) : ViewModel() {

    private val _componentState = MutableStateFlow<UiState<Component>>(UiState.Loading)
    val componentState: StateFlow<UiState<Component>> = _componentState.asStateFlow()

    private val _customerState = MutableStateFlow<UiState<Customer>>(UiState.Loading)
    val customerState: StateFlow<UiState<Customer>> = _customerState.asStateFlow()

    private val _moldState = MutableStateFlow<UiState<Mold>>(UiState.Loading)
    val moldState: StateFlow<UiState<Mold>> = _moldState.asStateFlow()

    fun loadComponent(id: String) {
        viewModelScope.launch {
            _componentState.value = UiState.Loading
            try {
                when (val result = useCases.getComponent(id)) {
                    is DataResult.Success -> _componentState.value = UiState.Success(result.data)
                    is DataResult.Error -> _componentState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _componentState.value = UiState.Error(e.message ?: "Failed to load component")
            }
        }
    }

    fun loadCustomer(id: String) {
        viewModelScope.launch {
            _customerState.value = UiState.Loading
            try {
                when (val result = useCases.getCustomer(id)) {
                    is DataResult.Success -> _customerState.value = UiState.Success(result.data)
                    is DataResult.Error -> _customerState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _customerState.value = UiState.Error(e.message ?: "Failed to load customer")
            }
        }
    }

    fun loadMold(id: String) {
        viewModelScope.launch {
            _moldState.value = UiState.Loading
            try {
                when (val result = useCases.getMold(id)) {
                    is DataResult.Success -> _moldState.value = UiState.Success(result.data)
                    is DataResult.Error -> _moldState.value = UiState.Error(result.message)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _moldState.value = UiState.Error(e.message ?: "Failed to load mold")
            }
        }
    }
}
