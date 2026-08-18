package dev.alfathrzqii.qrscreenscanner.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object WifiConnectionHelper {

    /**
     * Connects to the given Wi-Fi network and automatically copies the password to clipboard as backup.
     */
    fun connect(
        context: Context,
        ssid: String,
        password: String?,
        encryptionType: String?
    ) {
        val cleanSsid = ssid.trim()
        val cleanPassword = password?.trim() ?: ""

        // 1. Auto-copy password to clipboard as guaranteed backup
        if (cleanPassword.isNotEmpty()) {
            copyPasswordToClipboard(context, cleanSsid, cleanPassword)
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            openWifiSettings(context)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectAndroid10Plus(context, wifiManager, cleanSsid, cleanPassword, encryptionType)
            } else {
                connectLegacy(context, wifiManager, cleanSsid, cleanPassword, encryptionType)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Membuka Pengaturan Wi-Fi... (Sandi tersalin: $cleanPassword)",
                Toast.LENGTH_LONG
            ).show()
            openWifiSettings(context)
        }
    }

    private fun connectAndroid10Plus(
        context: Context,
        wifiManager: WifiManager,
        ssid: String,
        password: String,
        encryptionType: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val builder = WifiNetworkSuggestion.Builder().setSsid(ssid)

            val isWpa = encryptionType?.contains("WPA", ignoreCase = true) == true || encryptionType?.contains("WEP", ignoreCase = true) != true
            if (password.isNotEmpty() && isWpa) {
                builder.setWpa2Passphrase(password)
            } else if (password.isEmpty()) {
                builder.setIsEnhancedOpen(false)
            }

            val suggestion = builder.build()
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))

            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                Toast.makeText(
                    context,
                    "Jaringan \"$ssid\" disarankan ke sistem. Sandi tersalin ke papan klip.",
                    Toast.LENGTH_LONG
                ).show()

                // Open WiFi panel on Android 10+ for quick 1-tap connection confirm
                try {
                    val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(panelIntent)
                } catch (_: Exception) {
                    openWifiSettings(context)
                }
            } else {
                openWifiSettings(context)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun connectLegacy(
        context: Context,
        wifiManager: WifiManager,
        ssid: String,
        password: String,
        encryptionType: String?
    ) {
        val conf = WifiConfiguration().apply {
            SSID = "\"$ssid\""
        }

        if (password.isEmpty() || encryptionType?.contains("OPEN", ignoreCase = true) == true) {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else if (encryptionType?.contains("WEP", ignoreCase = true) == true) {
            conf.wepKeys[0] = "\"$password\""
            conf.wepTxKeyIndex = 0
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        } else {
            conf.preSharedKey = "\"$password\""
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        }

        val netId = wifiManager.addNetwork(conf)
        if (netId != -1) {
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            Toast.makeText(
                context,
                "Menghubungkan ke \"$ssid\"...",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            openWifiSettings(context)
        }
    }

    private fun copyPasswordToClipboard(context: Context, ssid: String, password: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wi-Fi Password: $ssid", password)
        clipboard.setPrimaryClip(clip)
    }

    fun openWifiSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka Pengaturan Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }
}
