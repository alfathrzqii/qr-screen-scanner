package dev.alfathrzqii.qrscreenscanner.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import java.net.URLEncoder

enum class WhatsAppPackage(val packageName: String, val appName: String) {
    REGULAR("com.whatsapp", "WhatsApp"),
    BUSINESS("com.whatsapp.w4b", "WhatsApp Business")
}

object SmartActionHandler {

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledWhatsAppPackages(context: Context): List<WhatsAppPackage> {
        val list = mutableListOf<WhatsAppPackage>()
        if (isPackageInstalled(context, WhatsAppPackage.REGULAR.packageName)) {
            list.add(WhatsAppPackage.REGULAR)
        }
        if (isPackageInstalled(context, WhatsAppPackage.BUSINESS.packageName)) {
            list.add(WhatsAppPackage.BUSINESS)
        }
        return list
    }

    /**
     * Directly opens WhatsApp or WhatsApp Business, or falls back to Web / native Chooser.
     * Supports opening via phone + message or direct action URL (e.g. group invites or channel links).
     */
    fun openWhatsApp(
        context: Context,
        rawPhone: String? = null,
        message: String? = null,
        actionUrl: String? = null,
        targetPackage: String? = null
    ) {
        val targetUrl = when {
            !rawPhone.isNullOrBlank() -> {
                val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "").let {
                    if (it.startsWith("08")) "628" + it.substring(2)
                    else if (it.startsWith("+")) it.substring(1)
                    else it
                }
                val encodedMessage = message?.let {
                    try { URLEncoder.encode(it, "UTF-8") } catch (_: Exception) { it }
                } ?: ""
                if (encodedMessage.isNotEmpty()) {
                    "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
                } else {
                    "https://api.whatsapp.com/send?phone=$cleanPhone"
                }
            }
            !actionUrl.isNullOrBlank() -> {
                if (!actionUrl.startsWith("http://") && !actionUrl.startsWith("https://") && !actionUrl.startsWith("whatsapp://")) {
                    "https://$actionUrl"
                } else {
                    actionUrl
                }
            }
            else -> "https://api.whatsapp.com"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!targetPackage.isNullOrBlank()) {
            intent.setPackage(targetPackage)
        } else {
            val installed = getInstalledWhatsAppPackages(context)
            if (installed.size == 1) {
                intent.setPackage(installed.first().packageName)
            }
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback: Launch general chooser/browser without specific package
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(fallbackIntent, "Buka di WhatsApp").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Launches Android's native Add Contact Form pre-filled with parsed contact data.
     */
    fun saveContact(
        context: Context,
        name: String?,
        phone: String?,
        email: String? = null,
        company: String? = null,
        jobTitle: String? = null,
        address: String? = null
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
                name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                company?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
                jobTitle?.let { putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it) }
                address?.let { putExtra(ContactsContract.Intents.Insert.POSTAL, it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka form kontak: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens a Chooser to send the QRIS / payment string to financial/payment applications.
     */
    fun openPaymentApp(context: Context, qrisPayload: String) {
        // First copy QRIS string to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QRIS Code", qrisPayload)
        clipboard.setPrimaryClip(clip)

        // Then launch payment intent chooser
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, qrisPayload)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Buka di Aplikasi Pembayaran / E-Wallet").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Kode QRIS disalin & siap digunakan di e-wallet", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Kode QRIS telah disalin ke papan klip", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat memanggil nomor: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String, subject: String? = null) {
        try {
            val uriBuilder = StringBuilder("mailto:$email")
            if (!subject.isNullOrBlank()) {
                uriBuilder.append("?subject=").append(Uri.encode(subject))
            }
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriBuilder.toString())).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat mengirim email: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
