package com.company.cavitrack.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryData(
    val components: List<Component> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val molds: List<Mold> = emptyList()
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<InventoryData>(isLoading = true))
    val uiState: StateFlow<UiState<InventoryData>> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = UiState(isLoading = true)
        viewModelScope.launch {
            try {
                repository.getComponents().collect { compRes ->
                    repository.getCustomers().collect { custRes ->
                        repository.getMolds().collect { moldRes ->
                            if (compRes is Result.Success && custRes is Result.Success && moldRes is Result.Success) {
                                _uiState.value = UiState(
                                    data = InventoryData(
                                        components = compRes.data,
                                        customers = custRes.data,
                                        molds = moldRes.data
                                    )
                                )
                            } else if (compRes is Result.Error) {
                                _uiState.value = UiState(error = compRes.message)
                            } else if (custRes is Result.Error) {
                                _uiState.value = UiState(error = custRes.message)
                            } else if (moldRes is Result.Error) {
                                _uiState.value = UiState(error = moldRes.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message ?: "Unknown error")
            }
        }
    }
}
