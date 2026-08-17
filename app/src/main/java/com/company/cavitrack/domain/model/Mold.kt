package com.company.cavitrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Mold(
    val id: String,
    val moldCode: String,
    val cavityCount: Int,
    val linkedComponentId: String? = null,
    val status: String, // Active, In Maintenance, Retired
    val location: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

