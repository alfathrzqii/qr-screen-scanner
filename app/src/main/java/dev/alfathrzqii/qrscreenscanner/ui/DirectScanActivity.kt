package dev.alfathrzqii.qrscreenscanner.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import dev.alfathrzqii.qrscreenscanner.ui.capture.ScreenCaptureActivity

/**
 * Lightweight translucent trampoline Activity designed as a secondary Launcher entry.
 * Can be selected directly in Xiaomi / Redmi Quick Ball (Bola Pintas), Samsung Edge Panel,
 * or pinned to the Home Screen to immediately launch the screen scanner without opening the dashboard.
 */
class DirectScanActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val captureIntent = Intent(this, ScreenCaptureActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(ScreenCaptureActivity.EXTRA_AUTO_TRIGGER, true)
        }

        startActivity(captureIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        finish()
    }
}
