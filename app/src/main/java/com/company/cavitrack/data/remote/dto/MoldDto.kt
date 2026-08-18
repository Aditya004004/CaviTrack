package com.company.cavitrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoldDto(
    val id: String = "",
    val moldCode: String = "",
    val cavityCount: Int = 0,
    val linkedComponentId: String? = null,
    val status: String = "",
    val location: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

