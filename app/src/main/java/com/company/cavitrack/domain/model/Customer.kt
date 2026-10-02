package com.company.cavitrack.domain.model

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val ownerId: String = "",
    val notes: String = "",
    val photoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
