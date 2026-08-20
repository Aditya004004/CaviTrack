package com.company.cavitrack.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable data object Home : Route()
    @Serializable data object Inventory : Route()
    @Serializable data object History : Route()
    @Serializable data object Settings : Route()
    @Serializable data object Login : Route()
    @Serializable data object Register : Route()
    @Serializable data class ComponentDetail(val id: String) : Route()
    @Serializable data class CustomerDetail(val id: String) : Route()
    @Serializable data class MoldDetail(val id: String) : Route()
    @Serializable data class AddUpdateAction(val entityType: String? = null) : Route()
    @Serializable data class ManualUpdate(val entityType: String, val entityId: String? = null) : Route()
    @Serializable data class PhotoUpdate(val entityType: String, val entityId: String? = null) : Route()
    @Serializable data class BarcodeScan(val entityType: String? = null) : Route()
}

