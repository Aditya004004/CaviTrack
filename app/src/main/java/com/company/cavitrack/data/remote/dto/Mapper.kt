package com.company.cavitrack.data.remote.dto

import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.model.MoldStatus

// Domain -> DTO

fun Component.toDto(): ComponentDto {
    return ComponentDto(
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
        updatedAt = updatedAt,
        isLowStock = qty < minStockThreshold
    )
}

fun Customer.toDto(): CustomerDto {
    return CustomerDto(
        id = id,
        ownerId = ownerId,
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

fun Mold.toDto() = MoldDto(
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

fun HistoryLog.toDto(): HistoryLogDto {
    return HistoryLogDto(
        id = id,
        ownerId = ownerId,
        entityType = entityType.name,
        entityId = entityId,
        entityName = entityName,
        action = action,
        changeSource = changeSource.name,
        changeNote = changeNote,
        beforeValue = beforeValue,
        afterValue = afterValue,
        photoUrl = photoUrl,
        performedBy = performedBy,
        timestamp = timestamp
    )
}

// DTO -> Domain

fun ComponentDto.toDomain(): Component {
    return Component(
        id = id,
        name = name,
        sku = sku,
        category = category,
        qty = qty,
        unit = unit,
        minStockThreshold = minStockThreshold,
        ownerId = ownerId,
        linkedMoldIds = linkedMoldIds,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CustomerDto.toDomain(): Customer {
    return Customer(
        id = id,
        name = name,
        phone = phone,
        email = email,
        address = address,
        ownerId = ownerId,
        linkedComponentIds = linkedComponentIds,
        notes = notes,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MoldDto.toDomain(): Mold {
    return Mold(
        id = id,
        moldCode = moldCode,
        cavityCount = cavityCount,
        status = try { MoldStatus.valueOf(status) } catch (_: Exception) { MoldStatus.Unknown },
        location = location,
        ownerId = ownerId,
        linkedComponentId = linkedComponentId,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun HistoryLogDto.toDomain(): HistoryLog {
    return HistoryLog(
        id = id,
        entityType = try { com.company.cavitrack.domain.model.EntityType.valueOf(entityType) } catch (_: Exception) { com.company.cavitrack.domain.model.EntityType.Component },
        entityId = entityId,
        entityName = entityName,
        action = action,
        changeSource = try { com.company.cavitrack.domain.model.ChangeSource.valueOf(changeSource) } catch (_: Exception) { com.company.cavitrack.domain.model.ChangeSource.Unknown },
        changeNote = changeNote,
        ownerId = ownerId,
        beforeValue = beforeValue,
        afterValue = afterValue,
        photoUrl = photoUrl,
        performedBy = performedBy,
        timestamp = timestamp
    )
}
