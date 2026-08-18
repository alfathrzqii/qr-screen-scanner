package com.alfath.qrscreenscanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QrContentType {
    URL,
    TEXT,
    WIFI,
    EMAIL,
    PHONE,
    SMS,
    GEO,
    OTHER
}

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawValue: String,
    val displayTitle: String,
    val contentType: QrContentType,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
