package com.example.beraparupiah.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Pipeline inference lengkap: YOLO Detection → Crop → MobileNetV2 Classification
 */
class InferencePipeline(private val context: Context) {

    private lateinit var yoloInterpreter: Interpreter
    private lateinit var mobileNetInterpreter: Interpreter
    private lateinit var classNames: List<String>

    companion object {
        private const val YOLO_MODEL = "best.tflite"
        private const val MOBILENET_MODEL = "best_model.tflite"
        private const val CLASS_NAMES = "class_names.json"

        // YOLO config
        private const val YOLO_INPUT_SIZE = 640
        private const val CONF_THRESHOLD = 0.5f
        private const val IOU_THRESHOLD = 0.45f

        // MobileNet config
        private const val MOBILENET_INPUT_SIZE = 224
    }

    /**
     * Initialize models
     */
    fun initialize() {
        // Load YOLO model
        val yoloModel = FileUtil.loadMappedFile(context, YOLO_MODEL)
        val yoloOptions = Interpreter.Options().apply {
            setNumThreads(4)
            // Gunakan GPU jika available (opsional)
            // addDelegate(GpuDelegate())
        }
        yoloInterpreter = Interpreter(yoloModel, yoloOptions)

        // Load MobileNet model
        val mobileNetModel = FileUtil.loadMappedFile(context, MOBILENET_MODEL)
        val mobileNetOptions = Interpreter.Options().apply {
            setNumThreads(4)
        }
        mobileNetInterpreter = Interpreter(mobileNetModel, mobileNetOptions)

        // Load class names
        classNames = loadClassNames()
    }

    /**
     * Main inference function
     */
    fun detectAndClassify(bitmap: Bitmap): DetectionResult {
        // Step 1: YOLO Detection
        val detections = detectWithYOLO(bitmap)

        // Step 2: Get best bounding box
        val bestBox = getBestBoundingBox(detections)

        // Step 3: Crop image
        val croppedBitmap = if (bestBox != null) {
            cropImage(bitmap, bestBox)
        } else {
            // Jika tidak ada deteksi, gunakan full image
            bitmap
        }

        // Step 4: Classify with MobileNetV2
        val (nominal, confidence, secondConfidence) =
            classifyWithMobileNet(croppedBitmap)


        return DetectionResult(
            nominal = nominal,
            confidence = confidence,
            secondConfidence = secondConfidence,
            boundingBox = bestBox,
            croppedBitmap = croppedBitmap,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * YOLO Object Detection
     */
    private fun detectWithYOLO(bitmap: Bitmap): List<BoundingBox> {
        // Preprocess image untuk YOLO
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            YOLO_INPUT_SIZE,
            YOLO_INPUT_SIZE,
            true
        )

        // Convert to ByteBuffer
        val inputBuffer = bitmapToByteBuffer(resizedBitmap, YOLO_INPUT_SIZE)

        // Prepare output
        // YOLO output shape: [1, 25200, 85] untuk COCO atau [1, 25200, 6] untuk custom
        // Format: [x, y, w, h, confidence, class_scores...]
        val outputShape = yoloInterpreter.getOutputTensor(0).shape()
        val numDetections = outputShape[1]
        val numClasses = outputShape[2] - 5

        val outputBuffer = Array(1) { Array(numDetections) { FloatArray(outputShape[2]) } }

        // Run inference
        yoloInterpreter.run(inputBuffer, outputBuffer)

        // Post-process: NMS (Non-Maximum Suppression)
        val detections = mutableListOf<BoundingBox>()
        val output = outputBuffer[0]

        for (i in output.indices) {
            val detection = output[i]
            val confidence = detection[4]

            if (confidence > CONF_THRESHOLD) {
                // Convert YOLO format to bounding box
                val centerX = detection[0]
                val centerY = detection[1]
                val width = detection[2]
                val height = detection[3]

                // Convert to corner coordinates
                val x1 = (centerX - width / 2) * bitmap.width / YOLO_INPUT_SIZE
                val y1 = (centerY - height / 2) * bitmap.height / YOLO_INPUT_SIZE
                val x2 = (centerX + width / 2) * bitmap.width / YOLO_INPUT_SIZE
                val y2 = (centerY + height / 2) * bitmap.height / YOLO_INPUT_SIZE

                detections.add(
                    BoundingBox(
                        x1 = x1,
                        y1 = y1,
                        x2 = x2,
                        y2 = y2,
                        confidence = confidence
                    )
                )
            }
        }

        // Apply NMS
        return nonMaxSuppression(detections)
    }

    /**
     * Get bounding box with highest confidence
     */
    private fun getBestBoundingBox(detections: List<BoundingBox>): BoundingBox? {
        return detections.maxByOrNull { it.confidence }
    }

    /**
     * Crop image based on bounding box
     */
    private fun cropImage(bitmap: Bitmap, box: BoundingBox): Bitmap {
        val x1 = max(0f, box.x1).toInt()
        val y1 = max(0f, box.y1).toInt()
        val x2 = box.x2.toInt().coerceAtMost(bitmap.width)
        val y2 = box.y2.toInt().coerceAtMost(bitmap.height)

        val width = x2 - x1
        val height = y2 - y1

        return Bitmap.createBitmap(bitmap, x1, y1, width, height)
    }

    /**
     * MobileNetV2 Classification
     */
    private fun classifyWithMobileNet(bitmap: Bitmap): Triple<String, Float, Float>
    {
        // Preprocess
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(MOBILENET_INPUT_SIZE, MOBILENET_INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to [0, 1]
            .build()

        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Run inference
        val outputBuffer = Array(1) { FloatArray(classNames.size) }
        mobileNetInterpreter.run(tensorImage.buffer, outputBuffer)

        // Get prediction
        val probabilities = outputBuffer[0]

        var firstIdx = -1
        var secondIdx = -1
        var firstConf = 0f
        var secondConf = 0f

        for (i in probabilities.indices) {
            val p = probabilities[i]

            if (p > firstConf) {
                secondConf = firstConf
                secondIdx = firstIdx

                firstConf = p
                firstIdx = i
            } else if (p > secondConf) {
                secondConf = p
                secondIdx = i
            }
        }

        val nominal = classNames[firstIdx]

        return Triple(nominal, firstConf, secondConf)

    }

    /**
     * Non-Maximum Suppression (NMS)
     */
    private fun nonMaxSuppression(boxes: List<BoundingBox>): List<BoundingBox> {
        if (boxes.isEmpty()) return emptyList()

        val sortedBoxes = boxes.sortedByDescending { it.confidence }
        val selectedBoxes = mutableListOf<BoundingBox>()

        for (box in sortedBoxes) {
            var shouldSelect = true

            for (selectedBox in selectedBoxes) {
                val iou = calculateIOU(box, selectedBox)
                if (iou > IOU_THRESHOLD) {
                    shouldSelect = false
                    break
                }
            }

            if (shouldSelect) {
                selectedBoxes.add(box)
            }
        }

        return selectedBoxes
    }

    /**
     * Calculate Intersection over Union (IOU)
     */
    private fun calculateIOU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = max(box1.x1, box2.x1)
        val y1 = max(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)

        val intersection = max(0f, x2 - x1) * max(0f, y2 - y1)

        val area1 = (box1.x2 - box1.x1) * (box1.y2 - box1.y1)
        val area2 = (box2.x2 - box2.x1) * (box2.y2 - box2.y1)
        val union = area1 + area2 - intersection

        return if (union > 0) intersection / union else 0f
    }

    /**
     * Convert Bitmap to ByteBuffer
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.rewind()

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            // Normalize to [0, 1]
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f) // R
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)  // G
            byteBuffer.putFloat((pixel and 0xFF) / 255f)          // B
        }

        return byteBuffer
    }

    /**
     * Load class names from JSON
     */
    private fun loadClassNames(): List<String> {
        val jsonString = context.assets.open(CLASS_NAMES).bufferedReader().use { it.readText() }
        // Parse JSON - contoh format: ["1000", "2000", "5000", ...]
        return jsonString
            .trim()
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
    }

    /**
     * Release resources
     */
    fun close() {
        yoloInterpreter.close()
        mobileNetInterpreter.close()
    }
}

/**
 * Data class untuk hasil deteksi
 */
data class DetectionResult(
    val nominal: String,
    val confidence: Float,
    val secondConfidence: Float,
    val boundingBox: BoundingBox?,
    val croppedBitmap: Bitmap?,
    val timestamp: Long
)

/**
 * Data class untuk bounding box
 */
data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val confidence: Float
)