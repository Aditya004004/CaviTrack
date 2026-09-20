package com.company.cavitrack.domain.usecase.inventory

import com.company.cavitrack.data.local.LocalMetricsRepository
import com.company.cavitrack.domain.model.ChangeSource
import com.company.cavitrack.domain.model.DashboardMetrics
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardMetricsUseCaseTest {

    private lateinit var repository: InventoryRepository
    private lateinit var localMetricsRepository: LocalMetricsRepository
    private lateinit var authRepository: com.company.cavitrack.domain.repository.AuthRepository
    private lateinit var useCase: GetDashboardMetricsUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        localMetricsRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { authRepository.getCurrentUserUid() } returns "test_user_id"
        every { localMetricsRepository.totalComponents } returns flowOf(10L)
        every { localMetricsRepository.lowStockCount } returns flowOf(2L)
        every { localMetricsRepository.totalCustomers } returns flowOf(5L)
        every { localMetricsRepository.activeMolds } returns flowOf(4L)
        every { localMetricsRepository.getTotalComponents("test_user_id") } returns flowOf(10L)
        every { localMetricsRepository.getLowStockCount("test_user_id") } returns flowOf(2L)
        every { localMetricsRepository.getTotalCustomers("test_user_id") } returns flowOf(5L)
        every { localMetricsRepository.getActiveMolds("test_user_id") } returns flowOf(4L)

        useCase = GetDashboardMetricsUseCase(repository, localMetricsRepository, authRepository)
    }

    @Test
    fun `useCase emits cached data first then server counts and history`() = runTest {
        // Arrange
        coEvery { repository.getComponentsCount() } returns DataResult.Success(15L)
        coEvery { repository.getLowStockComponentsCount() } returns DataResult.Success(3L)
        coEvery { repository.getCustomersCount() } returns DataResult.Success(8L)
        coEvery { repository.getActiveMoldsCount() } returns DataResult.Success(6L)
        every { repository.getRecentHistory(5) } returns flowOf(DataResult.Success(emptyList()))

        // Act
        val emissions = useCase().take(2).toList()

        // Assert
        assertTrue(emissions.isNotEmpty())
        val firstEmission = emissions.first()
        assertTrue(firstEmission is DataResult.Success)
        val cached = (firstEmission as DataResult.Success).data
        assertEquals(10, cached.totalComponents)
        assertEquals(2, cached.lowStockCount)
        assertEquals(5, cached.totalCustomers)
        assertEquals(4, cached.activeMolds)
    }

    @Test
    fun `useCase with uid emits user-scoped cached metrics`() = runTest {
        // Arrange
        val testUid = "user_123"
        every { localMetricsRepository.getTotalComponents(testUid) } returns flowOf(25L)
        every { localMetricsRepository.getLowStockCount(testUid) } returns flowOf(5L)
        every { localMetricsRepository.getTotalCustomers(testUid) } returns flowOf(12L)
        every { localMetricsRepository.getActiveMolds(testUid) } returns flowOf(8L)
        every { repository.getRecentHistory(5) } returns flowOf(DataResult.Success(emptyList()))

        // Act
        val emissions = useCase(testUid).take(1).toList()

        // Assert
        val firstEmission = emissions.first()
        assertTrue(firstEmission is DataResult.Success)
        val cached = (firstEmission as DataResult.Success).data
        assertEquals(25, cached.totalComponents)
        assertEquals(5, cached.lowStockCount)
        assertEquals(12, cached.totalCustomers)
        assertEquals(8, cached.activeMolds)
    }

    @Test
    fun `useCase recalculates counts dynamically when new history arrives`() = runTest {
        // Arrange
        val historyFlow = kotlinx.coroutines.flow.MutableSharedFlow<DataResult<List<HistoryLog>>>(extraBufferCapacity = 2)
        every { repository.getRecentHistory(5) } returns historyFlow

        coEvery { repository.getComponentsCount() } returnsMany listOf(
            DataResult.Success(10L),
            DataResult.Success(11L)
        )
        coEvery { repository.getLowStockComponentsCount() } returns DataResult.Success(2L)
        coEvery { repository.getCustomersCount() } returns DataResult.Success(5L)
        coEvery { repository.getActiveMoldsCount() } returns DataResult.Success(4L)

        val log = HistoryLog(
            id = "log1",
            entityType = EntityType.Component,
            entityId = "c1",
            entityName = "New Part",
            action = "Created",
            changeSource = ChangeSource.Manual,
            performedBy = "Tester",
            timestamp = 1000L
        )

        // Act
        val emissions = mutableListOf<DataResult<DashboardMetrics>>()
        val job = launch {
            useCase("test_user_id").collect { emissions.add(it) }
        }
        testScheduler.advanceUntilIdle()

        historyFlow.emit(DataResult.Success(listOf(log)))
        testScheduler.advanceUntilIdle()

        // Assert
        assertTrue(emissions.size >= 2)
        val latest = emissions.last()
        assertTrue(latest is DataResult.Success)
        val data = (latest as DataResult.Success<DashboardMetrics>).data
        assertEquals(11, data.totalComponents)
        assertEquals(1, data.recentActivity.size)
        assertEquals("New Part", data.recentActivity.first().entityName)

        job.cancel()
    }
}
