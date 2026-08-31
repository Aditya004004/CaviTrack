package com.company.cavitrack.data.remote.dto

import com.company.cavitrack.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperRoundTripTest {

    @Test
    fun `Component round trip mapping`() {
        val original = Component(
            id = "c1", ownerId = "u1", name = "Test Component", sku = "SKU-1",
            category = "Parts", qty = 100, unit = "pcs", minStockThreshold = 10,
            linkedMoldIds = listOf("m1", "m2"), photoUrl = "url", createdAt = 1L, updatedAt = 2L
        )

        val dto = original.toDto()
        val domainFromDto = dto.toDomain()

        assertEquals(original, domainFromDto)
    }

    @Test
    fun `Customer round trip mapping`() {
        val original = Customer(
            id = "c1", ownerId = "u1", name = "Customer", phone = "123", email = "email",
            address = "address", linkedComponentIds = listOf("c2"), notes = "note",
            photoUrl = "url", createdAt = 1L, updatedAt = 2L
        )

        val dto = original.toDto()
        val domainFromDto = dto.toDomain()

        assertEquals(original, domainFromDto)
    }

    @Test
    fun `Mold round trip mapping`() {
        val original = Mold(
            id = "m1", ownerId = "u1", moldCode = "M-1", cavityCount = 4,
            linkedComponentId = "c1", status = MoldStatus.Active, location = "A1",
            photoUrl = "url", createdAt = 1L, updatedAt = 2L
        )

        val dto = original.toDto()
        val domainFromDto = dto.toDomain()

        assertEquals(original, domainFromDto)
    }

    @Test
    fun `HistoryLog round trip mapping`() {
        val original = HistoryLog(
            id = "h1", ownerId = "u1", entityType = EntityType.Component, entityId = "c1",
            entityName = "Comp", action = "Update", changeSource = ChangeSource.Manual,
            beforeValue = "0", afterValue = "1", photoUrl = "url", performedBy = "user",
            timestamp = 1L
        )

        val dto = original.toDto()
        val domainFromDto = dto.toDomain()

        assertEquals(original, domainFromDto)
    }
}
