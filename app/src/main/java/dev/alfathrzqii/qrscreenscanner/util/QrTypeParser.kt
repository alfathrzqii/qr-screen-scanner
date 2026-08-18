package dev.alfathrzqii.qrscreenscanner.util

import android.net.Uri
import com.google.mlkit.vision.barcode.common.Barcode
import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import java.util.Locale

data class ParsedQrResult(
    val rawValue: String,
    val displayTitle: String,
    val subtitle: String? = null,
    val type: QrContentType,
    val actionUrl: String? = null,
    val qrisDetails: QrisDetails? = null,
    val wifiSsid: String? = null,
    val wifiPassword: String? = null,
    val wifiEncryption: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactOrg: String? = null,
    val contactTitle: String? = null,
    val contactAddress: String? = null,
    val whatsappPhone: String? = null,
    val whatsappMessage: String? = null
)

object QrTypeParser {

    fun parse(barcode: Barcode): ParsedQrResult {
        val rawValue = barcode.rawValue ?: barcode.displayValue ?: ""
        val trimmed = rawValue.trim()

        // 1. Check for QRIS / Digital Payment first (can be scanned as TEXT or URL by ML Kit)
        if (QrisParser.isQris(trimmed)) {
            val qris = QrisParser.parse(trimmed)
            val title = qris.merchantName ?: "Pembayaran QRIS"
            val sub = buildString {
                if (!qris.merchantCity.isNullOrBlank()) append(qris.merchantCity)
                if (!qris.formattedAmount.isNullOrBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(qris.formattedAmount)
                } else if (qris.isDynamic) {
                    if (isNotEmpty()) append(" • ")
                    append("QRIS Dinamis")
                } else {
                    if (isNotEmpty()) append(" • ")
                    append("QRIS Statis")
                }
            }
            return ParsedQrResult(
                rawValue = rawValue,
                displayTitle = title,
                subtitle = sub.ifBlank { null },
                type = QrContentType.QRIS,
                qrisDetails = qris
            )
        }

        // 2. Process by ML Kit Barcode Value Type
        return when (barcode.valueType) {
            Barcode.TYPE_CONTACT_INFO -> {
                val c = barcode.contactInfo
                val name = c?.name?.formattedName ?: c?.name?.first ?: "Kontak Baru"
                val phone = c?.phones?.firstOrNull()?.number
                val email = c?.emails?.firstOrNull()?.address
                val org = c?.organization
                val title = c?.title
                val address = c?.addresses?.firstOrNull()?.addressLines?.joinToString(", ")

                val subtitleStr = listOfNotNull(org, phone, email).joinToString(" • ")

                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = name,
                    subtitle = subtitleStr.ifBlank { null },
                    type = QrContentType.CONTACT,
                    contactName = name,
                    contactPhone = phone,
                    contactEmail = email,
                    contactOrg = org,
                    contactTitle = title,
                    contactAddress = address
                )
            }
            Barcode.TYPE_WIFI -> {
                val ssid = barcode.wifi?.ssid ?: "Jaringan Wi-Fi"
                val password = barcode.wifi?.password
                val encType = when (barcode.wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                    Barcode.WiFi.TYPE_WEP -> "WEP"
                    Barcode.WiFi.TYPE_OPEN -> "Open"
                    else -> "WPA/WPA2"
                }
                ParsedQrResult(
                    rawValue = rawValue,
                    displayTitle = ssid,
                    subtitle = "Keamanan: $encType" + (if (!password.isNullOrEmpty()) " • Dilindungi Sandi" else " • Terbuka"),
                    type = QrContentType.WIFI,
                    wifiSsid = ssid,
                    wifiPassword = password,
                    wifiEncryption = encType
                )
            }
            Barcode.TYPE_URL -> {
                val url = barcode.url?.url ?: rawValue
                if (isWhatsAppUrl(url)) {
                    parseWhatsApp(rawValue, url)
                } else {
                    ParsedQrResult(
                        rawValue = rawValue,
                        displayTitle = barcode.url?.title?.ifBlank { null } ?: formatDomain(url),
                        subtitle = url,
                        type = QrContentType.URL,
                        actionUrl = url
                    )
                }
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
                    subtitle = "Panggilan Telepon",
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

    fun parse(raw: String): ParsedQrResult {
        return parseRawString(raw)
    }

    private fun parseRawString(raw: String): ParsedQrResult {
        val trimmed = raw.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        return when {
            // QRIS check
            QrisParser.isQris(trimmed) -> {
                val qris = QrisParser.parse(trimmed)
                ParsedQrResult(
                    rawValue = raw,
                    displayTitle = qris.merchantName ?: "Pembayaran QRIS",
                    subtitle = listOfNotNull(qris.merchantCity, qris.formattedAmount ?: if (qris.isDynamic) "QRIS Dinamis" else "QRIS Statis").joinToString(" • "),
                    type = QrContentType.QRIS,
                    qrisDetails = qris
                )
            }
            // WhatsApp Link check
            isWhatsAppUrl(trimmed) -> {
                parseWhatsApp(raw, trimmed)
            }
            // vCard / MeCard
            lower.startsWith("begin:vcard") || lower.startsWith("mecard:") -> {
                parseVCardOrMeCard(raw)
            }
            // Wi-Fi raw string
            lower.startsWith("wifi:") -> {
                parseWifiString(raw)
            }
            // General URL
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

    private fun isWhatsAppUrl(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT)
        return lower.startsWith("https://wa.me") ||
                lower.startsWith("http://wa.me") ||
                lower.startsWith("wa.me") ||
                lower.contains("api.whatsapp.com") ||
                lower.startsWith("whatsapp://")
    }

    private fun parseWhatsApp(raw: String, url: String): ParsedQrResult {
        var phone = ""
        var message: String? = null

        try {
            val uri = Uri.parse(if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("whatsapp://")) "https://$url" else url)
            
            if (url.lowercase(Locale.ROOT).startsWith("whatsapp://")) {
                phone = uri.getQueryParameter("phone") ?: ""
                message = uri.getQueryParameter("text")
            } else if (uri.host?.contains("wa.me") == true) {
                phone = uri.path?.removePrefix("/") ?: ""
                message = uri.getQueryParameter("text")
            } else if (uri.host?.contains("api.whatsapp.com") == true) {
                phone = uri.getQueryParameter("phone") ?: ""
                message = uri.getQueryParameter("text")
            }
        } catch (_: Exception) {}

        val displayPhone = if (phone.isNotEmpty()) {
            if (phone.startsWith("62")) "+62 " + phone.substring(2) else phone
        } else {
            "Kontak WhatsApp"
        }

        return ParsedQrResult(
            rawValue = raw,
            displayTitle = "WhatsApp: $displayPhone",
            subtitle = message?.let { "\"$it\"" } ?: "Kirim pesan langsung via WhatsApp",
            type = QrContentType.WHATSAPP,
            actionUrl = url,
            whatsappPhone = phone,
            whatsappMessage = message
        )
    }

    private fun parseWifiString(raw: String): ParsedQrResult {
        // Format: WIFI:S:MySSID;T:WPA;P:MyPass;H:false;;
        val clean = raw.trim().removePrefix("WIFI:").removePrefix("wifi:")
        val parts = clean.split(";")
        var ssid = "Jaringan Wi-Fi"
        var password: String? = null
        var type = "WPA"

        for (part in parts) {
            val p = part.trim()
            when {
                p.startsWith("S:", ignoreCase = true) -> ssid = p.substring(2)
                p.startsWith("P:", ignoreCase = true) -> password = p.substring(2)
                p.startsWith("T:", ignoreCase = true) -> type = p.substring(2)
            }
        }

        return ParsedQrResult(
            rawValue = raw,
            displayTitle = ssid,
            subtitle = "Keamanan: $type" + (if (!password.isNullOrEmpty()) " • Dilindungi Sandi" else " • Terbuka"),
            type = QrContentType.WIFI,
            wifiSsid = ssid,
            wifiPassword = password,
            wifiEncryption = type
        )
    }

    private fun parseVCardOrMeCard(raw: String): ParsedQrResult {
        var name = "Kontak Baru"
        var phone: String? = null
        var email: String? = null
        var org: String? = null
        var title: String? = null

        val lines = raw.lines()
        for (line in lines) {
            val l = line.trim()
            val upper = l.uppercase(Locale.ROOT)
            when {
                upper.startsWith("FN:") || upper.startsWith("N:") -> if (name == "Kontak Baru") name = l.substringAfter(":")
                upper.startsWith("TEL:") || upper.startsWith("TEL;") -> if (phone == null) phone = l.substringAfter(":")
                upper.startsWith("EMAIL:") || upper.startsWith("EMAIL;") -> if (email == null) email = l.substringAfter(":")
                upper.startsWith("ORG:") -> if (org == null) org = l.substringAfter(":")
                upper.startsWith("TITLE:") -> if (title == null) title = l.substringAfter(":")
            }
        }

        // MeCard format: MECARD:N:Name;TEL:12345;EMAIL:a@b.com;;
        if (raw.trim().startsWith("MECARD:", ignoreCase = true)) {
            val parts = raw.trim().removePrefix("MECARD:").removePrefix("mecard:").split(";")
            for (part in parts) {
                val p = part.trim()
                when {
                    p.startsWith("N:", ignoreCase = true) -> name = p.substring(2)
                    p.startsWith("TEL:", ignoreCase = true) -> phone = p.substring(4)
                    p.startsWith("EMAIL:", ignoreCase = true) -> email = p.substring(6)
                    p.startsWith("ORG:", ignoreCase = true) -> org = p.substring(4)
                }
            }
        }

        val subtitleStr = listOfNotNull(org, phone, email).joinToString(" • ")

        return ParsedQrResult(
            rawValue = raw,
            displayTitle = name,
            subtitle = subtitleStr.ifBlank { null },
            type = QrContentType.CONTACT,
            contactName = name,
            contactPhone = phone,
            contactEmail = email,
            contactOrg = org,
            contactTitle = title
        )
    }

    private fun formatDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }
}
