package com.company.cavitrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ComponentDto(
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val qty: Int = 0,
    val unit: String = "",
    val minStockThreshold: Int = 0,
    val linkedMoldIds: List<String> = emptyList(),
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

