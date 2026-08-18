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
import kotlinx.coroutines.delay
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

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 4)
        var virtualDisplay: VirtualDisplay? = null
        val handler = Handler(Looper.getMainLooper())
        var latestValidBitmap: Bitmap? = null
        val frameReceived = CompletableDeferred<Boolean>()

        val projectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                try {
                    virtualDisplay?.release()
                } catch (_: Exception) {}
            }
        }
        mediaProjection.registerCallback(projectionCallback, handler)

        try {
            // Small delay to allow system permission dialog / scrim animation to dismiss completely
            delay(350)

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
                    if (image != null) {
                        val bitmap = convertImageToBitmap(image, width, height)
                        if (bitmap != null && !isBitmapAllBlank(bitmap)) {
                            latestValidBitmap = bitmap
                            if (!frameReceived.isCompleted) {
                                frameReceived.complete(true)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                }
            }, handler)

            // Wait up to 3 seconds for valid frame
            withTimeoutOrNull(3000L) {
                frameReceived.await()
            }

            // Brief pause to ensure we have the stabilized screen frame
            delay(150)

            return latestValidBitmap
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

    private fun convertImageToBitmap(image: Image, width: Int, height: Int): Bitmap? {
        return try {
            val planes = image.planes
            if (planes.isEmpty()) return null

            val buffer = planes[0].buffer ?: return null
            buffer.rewind()

            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width

            val bitmapWidth = width + (rowPadding / pixelStride)
            val bitmap = Bitmap.createBitmap(
                bitmapWidth,
                height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            if (rowPadding == 0 && bitmapWidth == width) {
                bitmap
            } else {
                val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                if (cropped != bitmap) {
                    bitmap.recycle()
                }
                cropped
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isBitmapAllBlank(bitmap: Bitmap): Boolean {
        return try {
            val w = bitmap.width
            val h = bitmap.height
            // Sample multiple points (corners, center, edges)
            val sample1 = bitmap.getPixel(w / 2, h / 2)
            val sample2 = bitmap.getPixel(w / 4, h / 4)
            val sample3 = bitmap.getPixel((w * 3) / 4, (h * 3) / 4)
            val sample4 = bitmap.getPixel(w / 4, (h * 3) / 4)
            val sample5 = bitmap.getPixel((w * 3) / 4, h / 4)

            // If completely transparent black 0
            sample1 == 0 && sample2 == 0 && sample3 == 0 && sample4 == 0 && sample5 == 0
        } catch (_: Exception) {
            false
        }
    }
}
