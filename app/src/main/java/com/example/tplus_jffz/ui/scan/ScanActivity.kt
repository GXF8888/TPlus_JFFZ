package com.example.tplus_jffz.ui.scan

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.tplus_jffz.R
import com.example.tplus_jffz.utils.PermissionsManager
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BARCODE = "extra_barcode"
        const val EXTRA_FORMAT = "extra_format"

        fun createIntent(context: Context): Intent {
            return Intent(context, ScanActivity::class.java)
        }
    }

    private lateinit var previewView: PreviewView
    private lateinit var barcodeScanner: BarcodeScanner
    private var mediaPlayer: MediaPlayer? = null
    private var isScanning = true
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        previewView = findViewById(R.id.preview_view)
        barcodeScanner = BarcodeScanning.getClient()
        mediaPlayer = MediaPlayer.create(this, R.raw.succeed)

        if (PermissionsManager.hasCameraPermission(this)) {
            startCamera()
        } else {
            PermissionsManager.requestCameraPermission(this)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isScanning) {
                            processImage(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("ScanActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && isScanning) {
                    isScanning = false
                    val barcode = barcodes[0]
                    val value = barcode.rawValue ?: ""
                    playBeep()
                    returnResult(value, barcode.format)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScanActivity", "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun playBeep() {
        mediaPlayer?.start()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    private fun returnResult(barcode: String, format: Int) {
        val intent = Intent().apply {
            putExtra(EXTRA_BARCODE, barcode)
            putExtra(EXTRA_FORMAT, format)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsManager.handlePermissionResult(
            requestCode, grantResults,
            onGranted = { startCamera() },
            onDenied = {
                Toast.makeText(this, "扫码功能需要相机权限", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        barcodeScanner.close()
    }
}
