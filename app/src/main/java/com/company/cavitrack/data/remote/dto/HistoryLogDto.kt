package com.company.cavitrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HistoryLogDto(
    val id: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val entityName: String = "",
    val action: String = "",
    val changeSource: String = "",
    val beforeValue: String? = null,
    val afterValue: String? = null,
    val photoUrl: String? = null,
    val performedBy: String = "Current User",
    val timestamp: Long = System.currentTimeMillis()
)

