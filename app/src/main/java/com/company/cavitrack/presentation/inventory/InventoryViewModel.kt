package com.company.cavitrack.presentation.inventory


import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    private val repository: InventoryRepository,
    sessionManager: com.company.cavitrack.util.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<InventoryData>>(UiState.Loading)
    val uiState: StateFlow<UiState<InventoryData>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.currentUser
                .distinctUntilChanged { old, new -> old?.uid == new?.uid }
                .collect { user ->
                    if (user != null) {
                        loadData()
                    } else {
                        loadJob?.cancel()
                        _uiState.value = UiState.Loading
                    }
                }
        }
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
                    if (compRes is DataResult.Error) return@combine UiState.Error(compRes.message)
                    if (custRes is DataResult.Error) return@combine UiState.Error(custRes.message)
                    if (moldRes is DataResult.Error) return@combine UiState.Error(moldRes.message)
                    
                    UiState.Success(
                        InventoryData(
                            components = (compRes as DataResult.Success).data,
                            customers = (custRes as DataResult.Success).data,
                            molds = (moldRes as DataResult.Success).data
                        )
                    )
                }.collect { combinedState ->
                    _uiState.value = combinedState
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}




