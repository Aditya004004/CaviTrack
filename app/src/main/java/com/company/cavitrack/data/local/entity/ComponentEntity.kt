package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.company.cavitrack.domain.model.Component

@Entity(
    tableName = "components",
    indices = [androidx.room.Index(value = ["ownerId", "sku"], unique = true)]
)
data class ComponentEntity(
    @PrimaryKey val id: String,
    val ownerId: String = "",
    val name: String,
    val sku: String,
    val category: String,
    val qty: Int,
    val unit: String,
    val minStockThreshold: Int,
    val linkedMoldIds: List<String>,
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun ComponentEntity.toDomain() = Component(
    id = id,
    ownerId = ownerId,
    name = name,
    sku = sku,
    category = category,
    qty = qty,
    unit = unit,
    minStockThreshold = minStockThreshold,
    linkedMoldIds = linkedMoldIds,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Component.toEntity() = ComponentEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    sku = sku,
    category = category,
    qty = qty,
    unit = unit,
    minStockThreshold = minStockThreshold,
    linkedMoldIds = linkedMoldIds,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)



