package com.company.cavitrack.presentation.home


import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val repository: InventoryRepository,
    sessionManager: com.company.cavitrack.util.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
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
            combine(
                repository.getComponents(),
                repository.getCustomers(),
                repository.getMolds(),
                repository.getHistory()
            ) { compRes, custRes, moldRes, histRes ->
                if (compRes is DataResult.Error) return@combine UiState.Error(compRes.message)
                if (custRes is DataResult.Error) return@combine UiState.Error(custRes.message)
                if (moldRes is DataResult.Error) return@combine UiState.Error(moldRes.message)
                if (histRes is DataResult.Error) return@combine UiState.Error(histRes.message)

                val components = (compRes as DataResult.Success).data
                val customers = (custRes as DataResult.Success).data
                val molds = (moldRes as DataResult.Success).data
                val history = (histRes as DataResult.Success).data

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




