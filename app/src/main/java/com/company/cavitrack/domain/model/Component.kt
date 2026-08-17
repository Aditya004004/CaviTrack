package com.company.cavitrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Component(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val qty: Int,
    val unit: String,
    val minStockThreshold: Int,
    val linkedMoldIds: List<String> = emptyList(),
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

