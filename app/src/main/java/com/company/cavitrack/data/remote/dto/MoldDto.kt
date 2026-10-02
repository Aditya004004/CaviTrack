package com.company.cavitrack.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@IgnoreExtraProperties
@Serializable
data class MoldDto @JvmOverloads constructor(
    val id: String = "",
    val ownerId: String = "",
    val moldCode: String = "",
    val moldCodeLower: String = "",
    val cavityCount: Int = 0,
    val status: String = "",
    val location: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false
)

