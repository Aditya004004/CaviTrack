package com.company.cavitrack.domain.model

enum class MoldStatus {
    Active,
    InMaintenance,
    Retired,
    Unknown
}

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
