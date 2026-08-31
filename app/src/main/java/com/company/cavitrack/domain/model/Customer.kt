package com.company.cavitrack.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val ownerId: String = "",
    val linkedComponentIds: List<String> = emptyList(),
    val notes: String = "",
    val photoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
