package com.company.cavitrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String, // CREATE, UPDATE, DELETE
    val entityType: String, // COMPONENT, CUSTOMER, MOLD
    val entityId: String,
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
