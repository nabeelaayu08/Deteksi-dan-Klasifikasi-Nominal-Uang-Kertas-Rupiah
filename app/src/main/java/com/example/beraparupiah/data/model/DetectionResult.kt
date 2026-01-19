package com.example.beraparupiah.data.model
import android.graphics.Bitmap

class DetectionResult {
    data class DetectionResult(
        val nominal: String,
        val confidence: Float,
        val boundingBox: BoundingBox?,
        val croppedBitmap: Bitmap?,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class BoundingBox(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val confidence: Float
    )
}