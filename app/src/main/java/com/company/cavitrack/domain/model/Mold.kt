package com.company.cavitrack.domain.model

import androidx.compose.runtime.Immutable

enum class MoldStatus {
    Active,
    InMaintenance,
    Retired,
    Unknown
}

@Immutable
data class Mold(
    val id: String,
    val moldCode: String,
    val cavityCount: Int,
    val status: MoldStatus,
    val location: String,
    val ownerId: String = "",
    val linkedComponentId: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
