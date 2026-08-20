package com.company.cavitrack.data.remote.dto

import com.company.cavitrack.data.local.entity.*
import com.company.cavitrack.domain.model.*

fun ComponentDto.toEntity(): ComponentEntity {
    return ComponentEntity(
        id = id,
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
}

fun Component.toDto(): ComponentDto {
    return ComponentDto(
        id = id,
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
}

fun CustomerDto.toEntity(): CustomerEntity {
    return CustomerEntity(
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
}

fun Customer.toDto(): CustomerDto {
    return CustomerDto(
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
}

fun MoldDto.toEntity(): MoldEntity {
    return MoldEntity(
        id = id,
        moldCode = moldCode,
        cavityCount = cavityCount,
        linkedComponentId = linkedComponentId,
        status = status,
        location = location,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Mold.toDto() = MoldDto(
    id = id,
    moldCode = moldCode,
    cavityCount = cavityCount,
    linkedComponentId = linkedComponentId,
    status = status.name,
    location = location,
    photoUrl = photoUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun HistoryLogDto.toEntity(): HistoryLogEntity {
    return HistoryLogEntity(
        id = id,
        entityType = entityType,
        entityId = entityId,
        entityName = entityName,
        action = action,
        changeSource = changeSource,
        beforeValue = beforeValue,
        afterValue = afterValue,
        photoUrl = photoUrl,
        performedBy = performedBy,
        timestamp = timestamp
    )
}
