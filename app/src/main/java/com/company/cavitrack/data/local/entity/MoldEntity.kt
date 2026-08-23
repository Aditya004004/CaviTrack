package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.company.cavitrack.domain.model.Mold

import com.company.cavitrack.domain.model.MoldStatus

@Entity(tableName = "molds")
data class MoldEntity(
    @PrimaryKey val id: String,
    val ownerId: String = "",
    val moldCode: String,
    val cavityCount: Int,
    val linkedComponentId: String?,
    val status: String,
    val location: String,
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun MoldEntity.toDomain() = Mold(
    id = id,
    ownerId = ownerId,
    moldCode = moldCode,
    cavityCount = cavityCount,
    linkedComponentId = linkedComponentId,
    status = try { MoldStatus.valueOf(status) } catch (e: Exception) { 
        android.util.Log.e("MoldEntity", "Unknown status: $status", e)
        MoldStatus.Unknown 
    },
    location = location,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Mold.toEntity() = MoldEntity(
    id = id,
    ownerId = ownerId,
    moldCode = moldCode,
    cavityCount = cavityCount,
    linkedComponentId = linkedComponentId,
    status = status.name,
    location = location,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
