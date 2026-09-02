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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val componentsFlow: Flow<PagingData<Component>> = combine(
        sessionManager.currentUser.map { it?.uid }.distinctUntilChanged(),
        _searchQuery,
        _lowStockOnly
    ) { uid, query, lowStock ->
        Triple(uid, query, lowStock)
    }.flatMapLatest { (uid, query, lowStock) ->
        if (uid != null) useCases.getComponents(query, lowStock) else flowOf(PagingData.empty())
    }.cachedIn(viewModelScope)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val customersFlow: Flow<PagingData<Customer>> = combine(
        sessionManager.currentUser.map { it?.uid }.distinctUntilChanged(),
        _searchQuery
    ) { uid, query ->
        Pair(uid, query)
    }.flatMapLatest { (uid, query) ->
        if (uid != null) useCases.getCustomers(query) else flowOf(PagingData.empty())
    }.cachedIn(viewModelScope)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val moldsFlow: Flow<PagingData<Mold>> = combine(
        sessionManager.currentUser.map { it?.uid }.distinctUntilChanged(),
        _searchQuery,
        _selectedMoldStatus
    ) { uid, query, status ->
        Triple(uid, query, status)
    }.flatMapLatest { (uid, query, status) ->
        if (uid != null) useCases.getMolds(query, status?.name) else flowOf(PagingData.empty())
    }.cachedIn(viewModelScope)

    fun loadData() {
        // Nothing to do for PagingData initialization
    }
}




