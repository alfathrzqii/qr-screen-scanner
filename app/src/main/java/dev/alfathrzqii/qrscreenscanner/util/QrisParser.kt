package dev.alfathrzqii.qrscreenscanner.util

import java.text.NumberFormat
import java.util.Locale

data class QrisDetails(
    val merchantName: String?,
    val merchantCity: String?,
    val postalCode: String?,
    val amount: String?,
    val formattedAmount: String?,
    val isDynamic: Boolean,
    val currency: String?,
    val countryCode: String?,
    val acquirers: List<String>,
    val rawValue: String
)

object QrisParser {

    /**
     * Checks if the given raw string matches the EMVCo / QRIS standard.
     */
    fun isQris(raw: String): Boolean {
        val trimmed = raw.trim()
        // QRIS specification starts with Tag "00" (Payload Format Indicator) with length 02 and value 01
        return trimmed.startsWith("000201") && (trimmed.contains("5802ID") || trimmed.contains("5303360") || trimmed.contains("ID.CO.QRIS.WWW") || trimmed.length >= 20)
    }

    /**
     * Parses the QRIS EMVCo Tag-Length-Value (TLV) string into structured details.
     */
    fun parse(raw: String): QrisDetails {
        val tags = parseTlv(raw.trim())

        val merchantName = tags["59"]
        val merchantCity = tags["60"]
        val postalCode = tags["61"]
        val rawAmount = tags["54"]
        val pointOfInitiation = tags["01"]
        val isDynamic = pointOfInitiation == "12"
        val currencyCode = tags["53"]
        val countryCode = tags["58"]

        val currency = when (currencyCode) {
            "360" -> "IDR"
            "840" -> "USD"
            "702" -> "SGD"
            "458" -> "MYR"
            else -> currencyCode
        }

        val formattedAmount = rawAmount?.toDoubleOrNull()?.let { amountVal ->
            try {
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                formatter.maximumFractionDigits = if (amountVal % 1.0 == 0.0) 0 else 2
                formatter.format(amountVal)
            } catch (_: Exception) {
                "Rp $rawAmount"
            }
        }

        // Acquirers / Merchant IDs from Tag 26 to 51
        val acquirers = mutableListOf<String>()
        for (i in 26..51) {
            val tagKey = String.format(Locale.US, "%02d", i)
            tags[tagKey]?.let { subTlvStr ->
                val subTags = parseTlv(subTlvStr)
                val domain = subTags["00"] ?: ""
                val nmid = subTags["01"] ?: ""
                val criteria = subTags["02"] ?: ""
                val label = when {
                    domain.contains("GOPAY", ignoreCase = true) -> "GoPay"
                    domain.contains("OVO", ignoreCase = true) -> "OVO"
                    domain.contains("DANA", ignoreCase = true) -> "DANA"
                    domain.contains("SHOPEE", ignoreCase = true) -> "ShopeePay"
                    domain.contains("LINKAJA", ignoreCase = true) -> "LinkAja"
                    domain.contains("QRIS", ignoreCase = true) -> "QRIS National"
                    domain.isNotEmpty() -> domain
                    else -> null
                }
                if (label != null && !acquirers.contains(label)) {
                    acquirers.add(label)
                }
            }
        }

        return QrisDetails(
            merchantName = merchantName,
            merchantCity = merchantCity,
            postalCode = postalCode,
            amount = rawAmount,
            formattedAmount = formattedAmount,
            isDynamic = isDynamic,
            currency = currency,
            countryCode = countryCode,
            acquirers = acquirers,
            rawValue = raw
        )
    }

    private fun parseTlv(payload: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var index = 0
        val length = payload.length

        while (index + 4 <= length) {
            val tag = payload.substring(index, index + 2)
            val lenStr = payload.substring(index + 2, index + 4)
            val valLength = lenStr.toIntOrNull() ?: break
            val valueStart = index + 4
            val valueEnd = valueStart + valLength

            if (valueEnd > length) break

            val value = payload.substring(valueStart, valueEnd)
            result[tag] = value
            index = valueEnd
        }

        return result
    }
}
