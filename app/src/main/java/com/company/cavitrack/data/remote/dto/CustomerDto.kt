package com.company.cavitrack.data.remote.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto @JvmOverloads constructor(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val linkedComponentIds: List<String> = emptyList(),
    val notes: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false
)

