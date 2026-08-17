package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.company.cavitrack.domain.model.HistoryLog

@Entity(tableName = "history_logs")
data class HistoryLogEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val entityName: String,
    val action: String,
    val changeSource: String,
    val beforeValue: String?,
    val afterValue: String?,
    val photoUrl: String?,
    val performedBy: String,
    val timestamp: Long
)

fun HistoryLogEntity.toDomain() = HistoryLog(
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

fun HistoryLog.toEntity() = HistoryLogEntity(
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
