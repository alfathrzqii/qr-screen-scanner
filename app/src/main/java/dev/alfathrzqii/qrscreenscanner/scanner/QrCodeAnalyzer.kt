package dev.alfathrzqii.qrscreenscanner.scanner

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

class QrCodeAnalyzer {

    private val scanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    suspend fun analyzeBitmap(bitmap: Bitmap): List<Barcode> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = scanner.process(image).await()
            barcodes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
