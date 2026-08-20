package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.company.cavitrack.domain.model.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val linkedComponentIds: List<String>,
    val notes: String,
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun CustomerEntity.toDomain() = Customer(
    id = id,
    name = name,
    phone = phone,
    email = email,
    address = address,
    linkedComponentIds = linkedComponentIds,
    notes = notes,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Customer.toEntity() = CustomerEntity(
    id = id,
    name = name,
    phone = phone,
    email = email,
    address = address,
    linkedComponentIds = linkedComponentIds,
    notes = notes,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
