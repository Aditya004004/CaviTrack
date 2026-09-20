package com.company.cavitrack.domain.usecase.inventory

import com.company.cavitrack.data.local.LocalMetricsRepository
import com.company.cavitrack.domain.model.DashboardMetrics
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetDashboardMetricsUseCase @Inject constructor(
    private val repository: InventoryRepository,
    private val localMetricsRepository: LocalMetricsRepository,
    private val authRepository: com.company.cavitrack.domain.repository.AuthRepository
) {
    operator fun invoke(uid: String? = null): Flow<DataResult<DashboardMetrics>> = flow {
        val effectiveUid = uid ?: authRepository.getCurrentUserUid() ?: ""
        // 1. Emit cached local metrics immediately
        val cachedComponents = (if (effectiveUid.isBlank()) localMetricsRepository.totalComponents else localMetricsRepository.getTotalComponents(effectiveUid)).first()
        val cachedLowStock = (if (effectiveUid.isBlank()) localMetricsRepository.lowStockCount else localMetricsRepository.getLowStockCount(effectiveUid)).first()
        val cachedCustomers = (if (effectiveUid.isBlank()) localMetricsRepository.totalCustomers else localMetricsRepository.getTotalCustomers(effectiveUid)).first()
        val cachedMolds = (if (effectiveUid.isBlank()) localMetricsRepository.activeMolds else localMetricsRepository.getActiveMolds(effectiveUid)).first()

        val initialData = DashboardMetrics(
            totalComponents = cachedComponents.toInt(),
            lowStockCount = cachedLowStock.toInt(),
            totalCustomers = cachedCustomers.toInt(),
            activeMolds = cachedMolds.toInt(),
            recentActivity = emptyList()
        )
        emit(DataResult.Success(initialData))

        // 2. Fetch fresh server counts once
        val freshCounts = fetchFreshCounts(effectiveUid)
        var activeCounts = freshCounts ?: initialData

        // 3. Stream history updates and re-fetch fresh counts when history logs change
        val liveFlow = repository.getRecentHistory(5).transform { historyResult ->
            when (historyResult) {
                is DataResult.Error -> emit(DataResult.Error(historyResult.message))
                is DataResult.Success -> {
                    val history = historyResult.data
                    val updatedCounts = fetchFreshCounts(effectiveUid) ?: activeCounts
                    activeCounts = updatedCounts
                    emit(DataResult.Success(activeCounts.copy(recentActivity = history)))
                }
            }
        }

        emitAll(liveFlow)
    }

    private suspend fun fetchFreshCounts(uid: String): DashboardMetrics? = coroutineScope {
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
                if (uid.isBlank()) {
                    localMetricsRepository.saveMetrics(
                        components = compRes.data,
                        lowStock = lowStockRes.data,
                        customers = custRes.data,
                        molds = moldRes.data
                    )
                } else {
                    localMetricsRepository.saveMetrics(
                        uid = uid,
                        components = compRes.data,
                        lowStock = lowStockRes.data,
                        customers = custRes.data,
                        molds = moldRes.data
                    )
                }

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
