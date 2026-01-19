package com.example.beraparupiah.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.beraparupiah.R
import com.example.beraparupiah.ml.InferencePipeline
import com.example.beraparupiah.viewmodel.DeteksiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.io.File
import java.io.FileOutputStream

class BerandaFragment : Fragment(), TextToSpeech.OnInitListener {
    private val MIN_CONFIDENCE = 0.60f
    private val MIN_MARGIN = 0.25f
    private lateinit var viewModel: DeteksiViewModel
    private lateinit var inferencePipeline: InferencePipeline
    private lateinit var tts: TextToSpeech

    // UI Components
    private lateinit var previewBox: FrameLayout
    private lateinit var imgPreview: ImageView
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var btnDetect: Button
    private lateinit var btnReset: Button
    private lateinit var txtResult: TextView
    private lateinit var txtConfidence: TextView
    private lateinit var progressBar: ProgressBar
    private var currentBitmap: Bitmap? = null
    private var ttsReady = false
    private var pilihGambarDialog: BottomSheetDialog? = null


    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                handleImageSelected(it)
            }
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val rawBitmap = BitmapFactory.decodeStream(inputStream)

                val fixedBitmap = fixBitmapRotationFromGallery(
                    rawBitmap,
                    uri
                )

                handleImageSelected(fixedBitmap)
            }
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        if (!cameraGranted) {
            Toast.makeText(requireContext(), "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        initializeViewModel()
        initializeInferencePipeline()
        initializeTTS()
        checkPermissions()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        previewBox = view.findViewById(R.id.preview_box)
        imgPreview = view.findViewById(R.id.img_preview)
        placeholderLayout = view.findViewById(R.id.placeholder_layout)
        btnDetect = view.findViewById(R.id.btnDetect)
        btnReset = view.findViewById(R.id.btn_reset)
        txtResult = view.findViewById(R.id.txtResult)
        txtConfidence = view.findViewById(R.id.txtConfidence)
        progressBar = view.findViewById(R.id.progress_bar)

        // Initial state
        btnDetect.isEnabled = false
        btnReset.visibility = View.GONE
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this)[DeteksiViewModel::class.java]
    }

    private fun initializeInferencePipeline() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                inferencePipeline = InferencePipeline(requireContext())
                inferencePipeline.initialize()
            }
        }
    }

    private fun initializeTTS() {
        tts = TextToSpeech(requireContext(), this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("id", "ID"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED

            if (!ttsReady) {
                Toast.makeText(
                    requireContext(),
                    "Bahasa Indonesia tidak tersedia untuk TTS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) !=
                    PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun setupListeners() {

        previewBox.setOnClickListener {
            if (currentBitmap == null) {
                showPilihGambarDialog()
            }
        }


        btnDetect.setOnClickListener {
            performDetection()
        }

        btnReset.setOnClickListener {
            resetState()
        }
    }

    private fun showPilihGambarDialog() {
        if (pilihGambarDialog == null) {
            val dialogView = layoutInflater.inflate(
                R.layout.dialog_pilih_gambar,
                null
            )

            val btnGaleri = dialogView.findViewById<LinearLayout>(R.id.btn_galeri)
            val btnKamera = dialogView.findViewById<LinearLayout>(R.id.btn_kamera)
            val btnBatal  = dialogView.findViewById<TextView>(R.id.btn_batal)

            btnGaleri.setOnClickListener {
                openGallery()
                pilihGambarDialog?.dismiss()
            }

            btnKamera.setOnClickListener {
                openCamera()
                pilihGambarDialog?.dismiss()
            }

            btnBatal.setOnClickListener {
                pilihGambarDialog?.dismiss()
            }

            pilihGambarDialog = BottomSheetDialog(requireContext())
            pilihGambarDialog?.setContentView(dialogView)
            pilihGambarDialog?.setCancelable(true)
        }

        pilihGambarDialog?.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    private fun handleImageSelected(bitmap: Bitmap) {
        currentBitmap = bitmap

        // Hide placeholder, show image
        placeholderLayout.visibility = View.GONE
        imgPreview.visibility = View.VISIBLE
        imgPreview.setImageBitmap(bitmap)

        // Enable detect button and show reset button
        btnDetect.isEnabled = true
        btnReset.visibility = View.VISIBLE
    }

    private fun fixBitmapRotationFromGallery(
        bitmap: Bitmap,
        uri: android.net.Uri
    ): Bitmap {

        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun performDetection() {
        val bitmap = currentBitmap ?: return

        // Show loading
        progressBar.visibility = View.VISIBLE
        btnDetect.isEnabled = false

        lifecycleScope.launch {
            try {
                // Run inference di background thread
                val result = withContext(Dispatchers.Default) {
                    inferencePipeline.detectAndClassify(bitmap)
                }

                // Update UI di main thread
                // Validasi apakah gambar benar-benar uang
                if (!isValidMoneyDetection(
                        result.nominal,
                        result.confidence,
                        result.secondConfidence
                    )) {

                    showInvalidImageNotification()  //notifikasi visual
                    resetAfterInvalid()     // reset UI
                    speakInvalidImage()        // notifikasi suara
                    return@launch
                }


// Jika valid → tampilkan hasil
                displayResult(result.nominal, result.confidence)


                // Save image
                val imagePath = saveImageToInternalStorage(currentBitmap!!)

                // Save to database
                viewModel.saveDetection(result.nominal, result.confidence, imagePath)

                // Play TTS
                speakResult(result.nominal, result.confidence)

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
                btnDetect.isEnabled = true
            }
        }
    }
    private fun isValidMoneyDetection(
        nominal: String,
        confidence: Float,
        secondConfidence: Float
    ): Boolean {

        val validNominals = listOf(
            "1000", "2000", "5000",
            "10000", "20000", "50000", "100000"
        )

        if (!validNominals.contains(nominal)) return false
        if (confidence < MIN_CONFIDENCE) return false

        val margin = confidence - secondConfidence
        if (margin < MIN_MARGIN) return false

        return true
    }

    private fun showInvalidImageNotification() {
        Toast.makeText(
            requireContext(),
            "Gambar yang Anda input tidak valid.\nSilakan masukkan gambar uang rupiah.",
            Toast.LENGTH_LONG
        ).show()
    }
    private fun resetAfterInvalid() {
        progressBar.visibility = View.GONE
        btnDetect.isEnabled = true
        txtResult.text = "Rp -"
        txtConfidence.text = "Confidence: -%"
    }

    private fun speakInvalidImage() {
        if (!ttsReady) return
        tts.speak(
            "Gambar yang Anda input tidak valid",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "INVALID_IMAGE"
        )
    }


    private fun displayResult(nominal: String, confidence: Float) {
        // Format nominal
        val formattedNominal = "Rp ${formatNominal(nominal)}"

        // Format confidence
        val confidencePercent = (confidence * 100).toInt()
        val formattedConfidence = "Confidence: $confidencePercent%"

        // Update UI
        txtResult.text = formattedNominal
        txtConfidence.text = formattedConfidence
    }

    private fun formatNominal(nominal: String): String {
        return try {
            val number = nominal.toLong()
            String.format(Locale("id", "ID"), "%,d", number).replace(',', '.')
        } catch (e: Exception) {
            nominal
        }
    }

    private fun speakResult(nominal: String, confidence: Float) {
        if (!ttsReady) return

        val text = buildTTSText(nominal, confidence)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun buildTTSText(nominal: String, confidence: Float): String {
        val nominalText = nominalToText(nominal)
        val confidenceText = confidenceToText(confidence)
        return "Terdeteksi uang $nominalText dengan akurasi $confidenceText"
    }

    private fun nominalToText(nominal: String): String {
        return when(nominal) {
            "1000" -> "seribu rupiah"
            "2000" -> "dua ribu rupiah"
            "5000" -> "lima ribu rupiah"
            "10000" -> "sepuluh ribu rupiah"
            "20000" -> "dua puluh ribu rupiah"
            "50000" -> "lima puluh ribu rupiah"
            "100000" -> "seratus ribu rupiah"
            else -> "nominal tidak dikenali"
        }
    }

    private fun confidenceToText(confidence: Float): String {
        val percent = (confidence * 100).toInt()
        return "$percent persen"
    }

    private fun resetState() {
        currentBitmap = null

        // Show placeholder, hide image
        placeholderLayout.visibility = View.VISIBLE
        imgPreview.visibility = View.GONE

        // Reset buttons
        btnDetect.isEnabled = false
        btnReset.visibility = View.GONE


        // Reset text
        txtResult.text = "Rp -"
        txtConfidence.text = "Confidence: -%"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        if (::inferencePipeline.isInitialized) {
            inferencePipeline.close()
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val timestamp = System.currentTimeMillis()
        val filename = "money_$timestamp.jpg"

        val file = File(requireContext().filesDir, filename)

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}