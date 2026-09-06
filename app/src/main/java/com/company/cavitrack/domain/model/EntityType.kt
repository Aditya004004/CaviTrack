package com.company.cavitrack.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class EntityType {
    Component, Customer, Mold, History
}
