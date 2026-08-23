package com.company.cavitrack.domain.model




data class HistoryLog(
    val id: String,
    val ownerId: String = "",
    val entityType: String, // Component, Customer, Mold
    val entityId: String,
    val entityName: String,
    val action: String, // Created, Updated, Stock Adjusted
    val changeSource: String, // Manual, Photo
    val beforeValue: String? = null,
    val afterValue: String? = null,
    val photoUrl: String? = null,
    val performedBy: String = "Current User",
    val timestamp: Long = System.currentTimeMillis()
)

