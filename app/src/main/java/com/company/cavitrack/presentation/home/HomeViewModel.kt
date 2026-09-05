package com.company.cavitrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.usecase.inventory.GetDashboardMetricsUseCase
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.util.DataResult
import com.company.cavitrack.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
    private val getDashboardMetrics: GetDashboardMetricsUseCase,
    sessionManager: SessionManager
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<UiState<HomeData>> = combine(
        sessionManager.currentUser.distinctUntilChanged { old, new -> old?.uid == new?.uid },
        retryTrigger.onStart { emit(Unit) }
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user != null) {
                getDashboardMetrics().map { result ->
                    when (result) {
                        is DataResult.Success -> UiState.Success(
                            HomeData(
                                totalComponents = result.data.totalComponents,
                                lowStockCount = result.data.lowStockCount,
                                totalCustomers = result.data.totalCustomers,
                                activeMolds = result.data.activeMolds,
                                recentActivity = result.data.recentActivity
                            )
                        )
                        is DataResult.Error -> UiState.Error(result.message)
                    }
                }.catch { e ->
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
