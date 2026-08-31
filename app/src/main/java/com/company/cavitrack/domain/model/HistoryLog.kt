package com.company.cavitrack.domain.model

enum class ChangeSource { Manual, Photo, Scanner, Unknown }

data class HistoryLog(
    val id: String,
    val entityType: EntityType,
    val entityId: String,
    val entityName: String,
    val action: String,
    val changeSource: ChangeSource,
    val changeNote: String? = null,
    val ownerId: String = "",
    val beforeValue: String? = null,
    val afterValue: String? = null,
    val photoUrl: String? = null,
    val performedBy: String = "",
    val timestamp: Long
)
