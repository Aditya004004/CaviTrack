package com.company.cavitrack.domain.model

data class DashboardMetrics(
    val totalComponents: Int = 0,
    val lowStockCount: Int = 0,
    val totalCustomers: Int = 0,
    val activeMolds: Int = 0,
    val recentActivity: List<HistoryLog> = emptyList()
)
