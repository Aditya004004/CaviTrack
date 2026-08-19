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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _uiState = MutableStateFlow<UiState<InventoryData>>(UiState.Loading)
    val uiState: StateFlow<UiState<InventoryData>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        loadData()
        viewModelScope.launch { repository.refreshData() }
    }

    fun loadData() {
        loadJob?.cancel()
        
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            try {
                combine(
                    repository.getComponents(),
                    repository.getCustomers(),
                    repository.getMolds()
                ) { compRes, custRes, moldRes ->
                    if (compRes is Result.Success && custRes is Result.Success && moldRes is Result.Success) {
                        UiState.Success(
                            InventoryData(
                                components = compRes.data,
                                customers = custRes.data,
                                molds = moldRes.data
                            )
                        )
                    } else if (compRes is Result.Error) {
                        UiState.Error(compRes.message)
                    } else if (custRes is Result.Error) {
                        UiState.Error(custRes.message)
                    } else if (moldRes is Result.Error) {
                        UiState.Error(moldRes.message)
                    } else {
                        UiState.Loading
                    }
                }.collect { combinedState ->
                    _uiState.value = combinedState
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

