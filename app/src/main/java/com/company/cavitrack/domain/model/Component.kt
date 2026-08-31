package com.company.cavitrack.domain.model

data class Component(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val qty: Int,
    val unit: String,
    val minStockThreshold: Int,
    val ownerId: String = "",
    val linkedMoldIds: List<String> = emptyList(),
    val photoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
