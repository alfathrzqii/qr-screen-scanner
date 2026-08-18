package com.alfath.qrscreenscanner.ui.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.alfath.qrscreenscanner.R
import com.alfath.qrscreenscanner.data.local.AppDatabase
import com.alfath.qrscreenscanner.data.local.ScanHistoryEntity
import com.alfath.qrscreenscanner.data.repository.ScanRepository
import com.alfath.qrscreenscanner.scanner.QrCodeAnalyzer
import com.alfath.qrscreenscanner.scanner.ScreenCaptureManager
import com.alfath.qrscreenscanner.service.MediaProjectionService
import com.alfath.qrscreenscanner.ui.components.MultiQrOverlay
import com.alfath.qrscreenscanner.ui.components.QrResultBottomSheet
import com.alfath.qrscreenscanner.ui.theme.QrScreenScannerTheme
import com.alfath.qrscreenscanner.util.HapticFeedbackHelper
import com.alfath.qrscreenscanner.util.ParsedQrResult
import com.alfath.qrscreenscanner.util.QrTypeParser
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureActivity : ComponentActivity() {

    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var projectionManager: MediaProjectionManager
    private val captureManager by lazy { ScreenCaptureManager(this) }
    private val qrAnalyzer by lazy { QrCodeAnalyzer() }
    private val repository by lazy {
        val db = AppDatabase.getInstance(applicationContext)
        ScanRepository(db.scanHistoryDao())
    }

    private var detectedBarcodes by mutableStateOf<List<Barcode>>(emptyList())
    private var activeResult by mutableStateOf<ParsedQrResult?>(null)
    private var showMultiOverlay by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        projectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                handleProjectionGranted(result.resultCode, result.data!!)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        setContent {
            QrScreenScannerTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showMultiOverlay && detectedBarcodes.size > 1) {
                        MultiQrOverlay(
                            barcodes = detectedBarcodes,
                            onSelectQr = { barcode ->
                                showMultiOverlay = false
                                processBarcodeResult(barcode)
                            },
                            onDismiss = {
                                finish()
                            }
                        )
                    }

                    activeResult?.let { result ->
                        QrResultBottomSheet(
                            result = result,
                            onDismiss = {
                                activeResult = null
                                finish()
                            }
                        )
                    }
                }
            }
        }

        // Request projection prompt immediately
        val captureIntent = projectionManager.createScreenCaptureIntent()
        projectionLauncher.launch(captureIntent)
    }

    private fun handleProjectionGranted(resultCode: Int, data: Intent) {
        lifecycleScope.launch {
            try {
                // 1. Start foreground service (Android 14+ requirement)
                MediaProjectionService.start(this@ScreenCaptureActivity)

                // 2. Obtain projection
                val mediaProjection = projectionManager.getMediaProjection(resultCode, data)
                if (mediaProjection == null) {
                    Toast.makeText(this@ScreenCaptureActivity, "Gagal menginisialisasi MediaProjection", Toast.LENGTH_SHORT).show()
                    MediaProjectionService.stop(this@ScreenCaptureActivity)
                    finish()
                    return@launch
                }

                // 3. Capture frame
                val bitmap = withContext(Dispatchers.Default) {
                    captureManager.captureScreenFrame(mediaProjection)
                }

                // 4. Stop foreground service
                MediaProjectionService.stop(this@ScreenCaptureActivity)

                if (bitmap == null) {
                    HapticFeedbackHelper.performErrorHaptic(this@ScreenCaptureActivity)
                    Toast.makeText(this@ScreenCaptureActivity, getString(R.string.no_qr_found), Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // 5. ML Kit Barcode Scan
                val barcodes = withContext(Dispatchers.Default) {
                    qrAnalyzer.analyzeBitmap(bitmap)
                }

                if (barcodes.isEmpty()) {
                    HapticFeedbackHelper.performErrorHaptic(this@ScreenCaptureActivity)
                    Toast.makeText(this@ScreenCaptureActivity, getString(R.string.no_qr_found), Toast.LENGTH_SHORT).show()
                    finish()
                } else if (barcodes.size == 1) {
                    HapticFeedbackHelper.performSuccessHaptic(this@ScreenCaptureActivity)
                    processBarcodeResult(barcodes.first())
                } else {
                    HapticFeedbackHelper.performSuccessHaptic(this@ScreenCaptureActivity)
                    detectedBarcodes = barcodes
                    showMultiOverlay = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ScreenCaptureActivity, "Terjadi kesalahan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun processBarcodeResult(barcode: Barcode) {
        val parsed = QrTypeParser.parse(barcode)
        activeResult = parsed

        // Save to Room DB asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            repository.insertScan(
                ScanHistoryEntity(
                    rawValue = parsed.rawValue,
                    displayTitle = parsed.displayTitle,
                    contentType = parsed.type,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        const val EXTRA_AUTO_TRIGGER = "EXTRA_AUTO_TRIGGER"
    }
}
