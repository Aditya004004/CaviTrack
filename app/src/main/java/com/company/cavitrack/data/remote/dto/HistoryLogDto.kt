package com.company.cavitrack.data.remote.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryLogDto @JvmOverloads constructor(
    val id: String = "",
    val ownerId: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val entityName: String = "",
    val action: String = "",
    val changeSource: String = "",
    val changeNote: String? = null,
    val beforeValue: String? = null,
    val afterValue: String? = null,
    val photoUrl: String? = null,
    val performedBy: String = "",
    val timestamp: Long = 0L,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false
)

