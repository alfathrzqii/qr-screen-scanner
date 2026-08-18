package dev.alfathrzqii.qrscreenscanner.ui.capture

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dev.alfathrzqii.qrscreenscanner.R
import dev.alfathrzqii.qrscreenscanner.data.local.AppDatabase
import dev.alfathrzqii.qrscreenscanner.data.local.ScanHistoryEntity
import dev.alfathrzqii.qrscreenscanner.data.repository.ScanRepository
import dev.alfathrzqii.qrscreenscanner.service.MediaProjectionService
import dev.alfathrzqii.qrscreenscanner.service.ScanResultEvent
import dev.alfathrzqii.qrscreenscanner.ui.components.MultiQrOverlay
import dev.alfathrzqii.qrscreenscanner.ui.components.QrResultBottomSheet
import dev.alfathrzqii.qrscreenscanner.ui.theme.QrScreenScannerTheme
import dev.alfathrzqii.qrscreenscanner.util.HapticFeedbackHelper
import dev.alfathrzqii.qrscreenscanner.util.ParsedQrResult
import dev.alfathrzqii.qrscreenscanner.util.QrTypeParser
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ScreenCaptureActivity : ComponentActivity() {

    private lateinit var projectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var projectionManager: MediaProjectionManager
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

        // Observe events emitted from MediaProjectionService
        lifecycleScope.launch {
            MediaProjectionService.scanEvents.collectLatest { event ->
                when (event) {
                    is ScanResultEvent.Success -> {
                        val barcodes = event.barcodes
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
                    }
                    is ScanResultEvent.Error -> {
                        HapticFeedbackHelper.performErrorHaptic(this@ScreenCaptureActivity)
                        Toast.makeText(this@ScreenCaptureActivity, event.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        projectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                MediaProjectionService.startCapture(this, result.resultCode, result.data!!)
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
