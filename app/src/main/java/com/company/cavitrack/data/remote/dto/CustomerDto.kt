package com.company.cavitrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val linkedComponentIds: List<String> = emptyList(),
    val notes: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
