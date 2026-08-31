package com.company.cavitrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EntityType {
    Component, Customer, Mold, History
}
