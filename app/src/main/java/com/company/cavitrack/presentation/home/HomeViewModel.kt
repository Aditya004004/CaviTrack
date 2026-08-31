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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

import com.company.cavitrack.util.SessionManager
import com.company.cavitrack.domain.usecase.inventory.GetDashboardMetricsUseCase

data class HomeData(
    val totalComponents: Int = 0,
    val lowStockCount: Int = 0,
    val totalCustomers: Int = 0,
    val activeMolds: Int = 0,
    val recentActivity: List<HistoryLog> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardMetrics: GetDashboardMetricsUseCase,
    sessionManager: SessionManager
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val uiState: StateFlow<UiState<HomeData>> = combine(
        sessionManager.currentUser.distinctUntilChanged { old, new -> old?.uid == new?.uid },
        retryTrigger.onStart { emit(Unit) }
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user != null) {
                getDashboardMetrics().catch { e ->
                    emit(UiState.Error(e.message ?: "Unknown error"))
                }
            } else {
                flowOf(UiState.Loading)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun loadData() {
        retryTrigger.tryEmit(Unit)
    }
}




