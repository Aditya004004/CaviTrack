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

fun com.company.cavitrack.domain.model.HistoryLog.toDto() = HistoryLogDto(
    id = id,
    entityType = entityType,
    entityId = entityId,
    entityName = entityName,
    action = action,
    changeSource = changeSource,
    beforeValue = beforeValue,
    afterValue = afterValue,
    photoUrl = photoUrl,
    performedBy = performedBy,
    timestamp = timestamp
)

fun HistoryLogDto.toEntity() = com.company.cavitrack.data.local.entity.HistoryLogEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    entityName = entityName,
    action = action,
    changeSource = changeSource,
    beforeValue = beforeValue,
    afterValue = afterValue,
    photoUrl = photoUrl,
    performedBy = performedBy,
    timestamp = timestamp
)

