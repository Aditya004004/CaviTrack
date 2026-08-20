package com.company.cavitrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MoldStatus {
    Active,
    InMaintenance,
    Retired
}

@Serializable
data class Mold(
    val id: String,
    val moldCode: String,
    val cavityCount: Int,
    val linkedComponentId: String? = null,
    val status: MoldStatus,
    val location: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

