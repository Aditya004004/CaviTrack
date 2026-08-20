package com.company.cavitrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.HistoryLog
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

data class HomeData(
    val totalComponents: Int = 0,
    val lowStockCount: Int = 0,
    val totalCustomers: Int = 0,
    val activeMolds: Int = 0,
    val recentActivity: List<HistoryLog> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        loadData()
        viewModelScope.launch { repository.refreshData() }
    }

    fun loadData() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            combine(
                repository.getComponents(),
                repository.getCustomers(),
                repository.getMolds(),
                repository.getHistory()
            ) { compRes, custRes, moldRes, histRes ->
                if (compRes is Result.Error) return@combine UiState.Error(compRes.message)
                if (custRes is Result.Error) return@combine UiState.Error(custRes.message)
                if (moldRes is Result.Error) return@combine UiState.Error(moldRes.message)
                if (histRes is Result.Error) return@combine UiState.Error(histRes.message)

                if (compRes is Result.Loading || custRes is Result.Loading || 
                    moldRes is Result.Loading || histRes is Result.Loading) {
                    return@combine UiState.Loading
                }

                val components = (compRes as Result.Success).data
                val customers = (custRes as Result.Success).data
                val molds = (moldRes as Result.Success).data
                val history = (histRes as Result.Success).data

                val lowStockCount = components.count { it.qty < it.minStockThreshold }
                val activeMolds = molds.count { it.status == com.company.cavitrack.domain.model.MoldStatus.Active }

                UiState.Success(
                    HomeData(
                        totalComponents = components.size,
                        lowStockCount = lowStockCount,
                        totalCustomers = customers.size,
                        activeMolds = activeMolds,
                        recentActivity = history.take(5)
                    )
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

