package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.company.cavitrack.domain.model.Component

@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val qty: Int,
    val unit: String,
    val minStockThreshold: Int,
    val linkedMoldIds: String, // Stored as comma-separated string for simplicity
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun ComponentEntity.toDomain() = Component(
    id = id,
    name = name,
    sku = sku,
    category = category,
    qty = qty,
    unit = unit,
    minStockThreshold = minStockThreshold,
    linkedMoldIds = if (linkedMoldIds.isEmpty()) emptyList() else linkedMoldIds.split(","),
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Component.toEntity() = ComponentEntity(
    id = id,
    name = name,
    sku = sku,
    category = category,
    qty = qty,
    unit = unit,
    minStockThreshold = minStockThreshold,
    linkedMoldIds = linkedMoldIds.joinToString(","),
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
