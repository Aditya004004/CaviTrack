package com.company.cavitrack.domain.usecase.inventory

import com.company.cavitrack.data.local.LocalMetricsRepository
import com.company.cavitrack.domain.model.DashboardMetrics
import com.company.cavitrack.domain.repository.InventoryRepository
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
    operator fun invoke(): Flow<DataResult<DashboardMetrics>> = flow {
        // 1. Emit cached local metrics immediately
        val cachedComponents = localMetricsRepository.totalComponents.first()
        val cachedLowStock = localMetricsRepository.lowStockCount.first()
        val cachedCustomers = localMetricsRepository.totalCustomers.first()
        val cachedMolds = localMetricsRepository.activeMolds.first()

        val initialData = DashboardMetrics(
            totalComponents = cachedComponents.toInt(),
            lowStockCount = cachedLowStock.toInt(),
            totalCustomers = cachedCustomers.toInt(),
            activeMolds = cachedMolds.toInt(),
            recentActivity = emptyList()
        )
        emit(DataResult.Success(initialData))

        // 2. Combine history flow and fetch fresh server counts
        val combinedFlow = repository.getRecentHistory(5).combine(flow {
            emit(fetchFreshCounts())
        }) { historyResult, countsResult ->
            if (historyResult is DataResult.Error) {
                DataResult.Error(historyResult.message)
            } else {
                val history = (historyResult as DataResult.Success).data
                if (countsResult != null) {
                    DataResult.Success(
                        DashboardMetrics(
                            totalComponents = countsResult.totalComponents,
                            lowStockCount = countsResult.lowStockCount,
                            totalCustomers = countsResult.totalCustomers,
                            activeMolds = countsResult.activeMolds,
                            recentActivity = history
                        )
                    )
                } else {
                    DataResult.Success(
                        initialData.copy(recentActivity = history)
                    )
                }
            }
        }

        emitAll(combinedFlow)
    }

    private suspend fun fetchFreshCounts(): DashboardMetrics? = coroutineScope {
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
                custRes is DataResult.Success && moldRes is DataResult.Success
            ) {
                localMetricsRepository.saveMetrics(
                    components = compRes.data,
                    lowStock = lowStockRes.data,
                    customers = custRes.data,
                    molds = moldRes.data
                )

                return@coroutineScope DashboardMetrics(
                    totalComponents = compRes.data.toInt(),
                    lowStockCount = lowStockRes.data.toInt(),
                    totalCustomers = custRes.data.toInt(),
                    activeMolds = moldRes.data.toInt(),
                    recentActivity = emptyList()
                )
            }
            null
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null // Fails gracefully offline
        }
    }
}
