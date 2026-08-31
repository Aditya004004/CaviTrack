package com.company.cavitrack.domain.usecase.inventory

import com.company.cavitrack.data.local.LocalMetricsRepository
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.home.HomeData
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetDashboardMetricsUseCase @Inject constructor(
    private val repository: InventoryRepository,
    private val localMetricsRepository: LocalMetricsRepository
) {
    operator fun invoke(): Flow<UiState<HomeData>> = flow {
        emit(UiState.Loading)
        
        // 1. Emit cached local metrics immediately
        val cachedComponents = localMetricsRepository.totalComponents.first()
        val cachedLowStock = localMetricsRepository.lowStockCount.first()
        val cachedCustomers = localMetricsRepository.totalCustomers.first()
        val cachedMolds = localMetricsRepository.activeMolds.first()
        
        // We might not have cached history logs, so we fetch those from the flow below
        // but we can emit the cached counts first.
        val initialData = HomeData(
            totalComponents = cachedComponents.toInt(),
            lowStockCount = cachedLowStock.toInt(),
            totalCustomers = cachedCustomers.toInt(),
            activeMolds = cachedMolds.toInt(),
            recentActivity = emptyList() // Will be updated when stream comes in
        )
        emit(UiState.Success(initialData))

        // 2. Combine history flow and fetch fresh server counts
        val combinedFlow = repository.getRecentHistory(5).combine(flow {
            emit(fetchFreshCounts())
        }) { historyResult, countsResult ->
            if (historyResult is DataResult.Error) {
                UiState.Error(historyResult.message)
            } else {
                val history = (historyResult as DataResult.Success).data
                
                if (countsResult != null) {
                    // Fresh counts retrieved, combine with history
                    UiState.Success(
                        HomeData(
                            totalComponents = countsResult.totalComponents.toInt(),
                            lowStockCount = countsResult.lowStockCount.toInt(),
                            totalCustomers = countsResult.totalCustomers.toInt(),
                            activeMolds = countsResult.activeMolds.toInt(),
                            recentActivity = history
                        )
                    )
                } else {
                    // Network failed for counts, use cached counts + history
                    UiState.Success(
                        initialData.copy(recentActivity = history)
                    )
                }
            }
        }
        
        emitAll(combinedFlow)
    }

    private suspend fun fetchFreshCounts(): HomeData? = coroutineScope {
        try {
            val compDeferred = async { repository.getComponentsCount() }
            val lowStockDeferred = async { repository.getLowStockComponentsCount() }
            val custDeferred = async { repository.getCustomersCount() }
            val moldDeferred = async { repository.getActiveMoldsCount() }

            val compRes = compDeferred.await()
            val lowStockRes = lowStockDeferred.await()
            val custRes = custDeferred.await()
            val moldRes = moldDeferred.await()

            if (compRes is DataResult.Success && lowStockRes is DataResult.Success && 
                custRes is DataResult.Success && moldRes is DataResult.Success) {
                
                // Save to local cache
                localMetricsRepository.saveMetrics(
                    components = compRes.data,
                    lowStock = lowStockRes.data,
                    customers = custRes.data,
                    molds = moldRes.data
                )
                
                return@coroutineScope HomeData(
                    totalComponents = compRes.data.toInt(),
                    lowStockCount = lowStockRes.data.toInt(),
                    totalCustomers = custRes.data.toInt(),
                    activeMolds = moldRes.data.toInt(),
                    recentActivity = emptyList() // Merged later
                )
            }
            null
        } catch (e: Exception) {
            null // Fails gracefully offline
        }
    }
}
