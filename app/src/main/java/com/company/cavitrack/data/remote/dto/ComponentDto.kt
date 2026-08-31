package com.company.cavitrack.data.remote.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class ComponentDto @JvmOverloads constructor(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val qty: Int = 0,
    val unit: String = "",
    val minStockThreshold: Int = 0,
    val linkedMoldIds: List<String> = emptyList(),
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean = false,
    val isLowStock: Boolean = false
)

