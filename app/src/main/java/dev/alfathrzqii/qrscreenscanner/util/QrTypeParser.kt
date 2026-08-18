package dev.alfathrzqii.qrscreenscanner.util

import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.Locale

data class ParsedQrResult(
    val rawValue: String,
    val displayTitle: String,
    val subtitle: String? = null,
    val type: QrContentType,
    val actionUrl: String? = null
)

object QrTypeParser {

    fun parse(barcode: Barcode): ParsedQrResult {
        val rawValue = barcode.rawValue ?: barcode.displayValue ?: ""

        return when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                val url = barcode.url?.url ?: rawValue
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = barcode.url?.title?.ifBlank { null } ?: formatDomain(url),
                    subtitle = url,
                    type = QrContentType.URL,
                    actionUrl = url
                )
            }
            Barcode.TYPE_WIFI -> {
                val ssid = barcode.wifi?.ssid ?: "Jaringan Wi-Fi"
                val encryptionType = when (barcode.wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                    Barcode.WiFi.TYPE_WEP -> "WEP"
                    Barcode.WiFi.TYPE_OPEN -> "Open (Tanpa Sandi)"
                    else -> "Wi-Fi"
                }
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = ssid,
                    subtitle = "Keamanan: $encryptionType",
                    type = QrContentType.WIFI
                )
            }
            Barcode.TYPE_EMAIL -> {
                val email = barcode.email?.address ?: rawValue
                val subject = barcode.email?.subject
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = email,
                    subtitle = if (!subject.isNullOrBlank()) "Subjek: $subject" else "Kirim Email",
                    type = QrContentType.EMAIL,
                    actionUrl = "mailto:$email"
                )
            }
            Barcode.TYPE_PHONE -> {
                val phone = barcode.phone?.number ?: rawValue
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = phone,
                    subtitle = "Panggilan Suara",
                    type = QrContentType.PHONE,
                    actionUrl = "tel:$phone"
                )
            }
            Barcode.TYPE_SMS -> {
                val phone = barcode.sms?.phoneNumber ?: ""
                val msg = barcode.sms?.message ?: ""
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = if (phone.isNotEmpty()) "SMS ke $phone" else "Pesan SMS",
                    subtitle = if (msg.isNotEmpty()) msg else null,
                    type = QrContentType.SMS,
                    actionUrl = "smsto:$phone"
                )
            }
            Barcode.TYPE_GEO -> {
                val lat = barcode.geoPoint?.lat ?: 0.0
                val lng = barcode.geoPoint?.lng ?: 0.0
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = "Titik Koordinat Lokasi",
                    subtitle = "$lat, $lng",
                    type = QrContentType.GEO,
                    actionUrl = "geo:$lat,$lng"
                )
            }
            else -> {
                parseRawString(rawValue)
            }
        }
    }

    private fun parseRawString(raw: String): ParsedQrResult {
        val trimmed = raw.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        return when {
            lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.") -> {
                val url = if (lower.startsWith("www.")) "https://$trimmed" else trimmed
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = formatDomain(url),
                    subtitle = url,
                    type = QrContentType.URL,
                    actionUrl = url
                )
            }
            lower.startsWith("wifi:") -> {
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = "Konfigurasi Wi-Fi",
                    subtitle = trimmed,
                    type = QrContentType.WIFI
                )
            }
            lower.startsWith("mailto:") -> {
                val email = trimmed.substring(7)
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = email,
                    subtitle = "Kirim Pesan Email",
                    type = QrContentType.EMAIL,
                    actionUrl = trimmed
                )
            }
            lower.startsWith("tel:") -> {
                val phone = trimmed.substring(4)
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = phone,
                    subtitle = "Panggilan Telepon",
                    type = QrContentType.PHONE,
                    actionUrl = trimmed
                )
            }
            else -> {
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = if (trimmed.length > 50) trimmed.take(50) + "…" else trimmed,
                    subtitle = null,
                    type = QrContentType.TEXT
                )
            }
        }
    }

    private fun formatDomain(url: String): String {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }
}
