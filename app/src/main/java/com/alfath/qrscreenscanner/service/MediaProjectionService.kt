package com.alfath.qrscreenscanner.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.alfath.qrscreenscanner.R
import com.alfath.qrscreenscanner.scanner.QrCodeAnalyzer
import com.alfath.qrscreenscanner.scanner.ScreenCaptureManager
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScanResultEvent {
    data class Success(val barcodes: List<Barcode>) : ScanResultEvent()
    data class Error(val message: String) : ScanResultEvent()
}

class MediaProjectionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val captureManager by lazy { ScreenCaptureManager(applicationContext) }
    private val qrAnalyzer by lazy { QrCodeAnalyzer() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        @Suppress("DEPRECATION")
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        // 1. Enter foreground IMMEDIATELY with mediaProjection type
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (resultCode == 0 || resultData == null) {
            serviceScope.launch {
                _scanEvents.emit(ScanResultEvent.Error("Data izin tangkapan layar tidak valid"))
                stopSelf()
            }
            return START_NOT_STICKY
        }

        // 2. Obtain projection while strictly running in foreground
        serviceScope.launch {
            try {
                val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

                if (mediaProjection == null) {
                    _scanEvents.emit(ScanResultEvent.Error("Gagal menginisialisasi MediaProjection"))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                // 3. Capture frame bitmap
                val bitmap = withContext(Dispatchers.Default) {
                    captureManager.captureScreenFrame(mediaProjection)
                }

                if (bitmap == null) {
                    _scanEvents.emit(ScanResultEvent.Error(getString(R.string.no_qr_found)))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                // 4. Decode barcodes
                val barcodes = withContext(Dispatchers.Default) {
                    qrAnalyzer.analyzeBitmap(bitmap)
                }

                _scanEvents.emit(ScanResultEvent.Success(barcodes))
            } catch (e: Exception) {
                e.printStackTrace()
                _scanEvents.emit(ScanResultEvent.Error("Kesalahan: ${e.localizedMessage}"))
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_service_running))
            .setSmallIcon(R.drawable.ic_qr_scan_tile)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "qr_screen_capture_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_PROJECTION"
        const val ACTION_STOP = "ACTION_STOP_PROJECTION"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"

        private val _scanEvents = MutableSharedFlow<ScanResultEvent>(extraBufferCapacity = 1)
        val scanEvents: SharedFlow<ScanResultEvent> = _scanEvents.asSharedFlow()

        fun startCapture(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, MediaProjectionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
