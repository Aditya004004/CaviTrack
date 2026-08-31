package com.company.cavitrack.presentation.inventory


import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.usecase.inventory.InventoryUseCases
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.paging.PagingData
import androidx.paging.cachedIn
import javax.inject.Inject

data class InventoryData(
    val components: List<Component> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val molds: List<Mold> = emptyList()
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val useCases: InventoryUseCases,
    sessionManager: com.company.cavitrack.util.SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lowStockOnly = MutableStateFlow(false)
    val lowStockOnly: StateFlow<Boolean> = _lowStockOnly.asStateFlow()

    private val _selectedMoldStatus = MutableStateFlow<com.company.cavitrack.domain.model.MoldStatus?>(null)
    val selectedMoldStatus: StateFlow<com.company.cavitrack.domain.model.MoldStatus?> = _selectedMoldStatus.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateLowStockFilter(lowStock: Boolean) {
        _lowStockOnly.value = lowStock
    }

    fun updateMoldStatusFilter(status: com.company.cavitrack.domain.model.MoldStatus?) {
        _selectedMoldStatus.value = status
    }

    val componentsFlow: Flow<PagingData<Component>> = useCases.getComponents().cachedIn(viewModelScope)
    val customersFlow: Flow<PagingData<Customer>> = useCases.getCustomers().cachedIn(viewModelScope)
    val moldsFlow: Flow<PagingData<Mold>> = useCases.getMolds().cachedIn(viewModelScope)

    fun loadData() {
        // Nothing to do for PagingData initialization
    }
}




