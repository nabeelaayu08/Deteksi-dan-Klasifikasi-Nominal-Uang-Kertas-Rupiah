package com.example.beraparupiah.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riwayat_deteksi")
data class RiwayatDeteksi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val nominal: String,
    val confidence: Float,
    val timestamp: Long,
    val imagePath: String? = null
)