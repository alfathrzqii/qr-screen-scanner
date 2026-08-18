package com.alfath.qrscreenscanner.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

class ScreenCaptureManager(private val context: Context) {

    suspend fun captureScreenFrame(mediaProjection: MediaProjection): Bitmap? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        var virtualDisplay: VirtualDisplay? = null
        val deferredBitmap = CompletableDeferred<Bitmap?>()
        val handler = Handler(Looper.getMainLooper())

        val projectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                try {
                    virtualDisplay?.release()
                } catch (_: Exception) {}
            }
        }
        mediaProjection.registerCallback(projectionCallback, handler)

        try {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "QrScreenCaptureDisplay",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                handler
            )

            imageReader.setOnImageAvailableListener({ reader ->
                var image: Image? = null
                try {
                    image = reader.acquireLatestImage()
                    if (image != null && !deferredBitmap.isCompleted) {
                        val bitmap = convertImageToBitmap(image, width, height)
                        deferredBitmap.complete(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (!deferredBitmap.isCompleted) {
                        deferredBitmap.complete(null)
                    }
                } finally {
                    image?.close()
                }
            }, handler)

            // Wait with a 2.5 second timeout to acquire frame
            return withTimeoutOrNull(2500L) {
                deferredBitmap.await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                virtualDisplay?.release()
                imageReader.close()
                mediaProjection.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun convertImageToBitmap(image: Image, width: Int, height: Int): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        return if (rowPadding == 0) {
            bitmap
        } else {
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            if (cropped != bitmap) {
                bitmap.recycle()
            }
            cropped
        }
    }
}
