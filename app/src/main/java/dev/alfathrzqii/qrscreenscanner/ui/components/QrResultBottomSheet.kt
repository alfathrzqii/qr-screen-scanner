package dev.alfathrzqii.qrscreenscanner.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import dev.alfathrzqii.qrscreenscanner.util.ParsedQrResult
import dev.alfathrzqii.qrscreenscanner.util.SmartActionHandler
import dev.alfathrzqii.qrscreenscanner.util.WhatsAppPackage
import dev.alfathrzqii.qrscreenscanner.util.WifiConnectionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrResultBottomSheet(
    result: ParsedQrResult,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surfaceContainerHigh,
        scrimColor = Color(0x99000000),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            // Header Section: Icon Badge + Category info
            val (icon, badgeLabel, badgeColor) = getCategoryMeta(result.type)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(badgeColor ?: colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badgeLabel,
                        tint = if (badgeColor != null) Color.White else colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = badgeLabel.uppercase(),
                            color = colorScheme.onSecondaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = result.displayTitle,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Smart Action Cards tailored to specific QR types
            when (result.type) {
                QrContentType.QRIS -> {
                    QrisCard(result = result, onDismiss = onDismiss)
                }
                QrContentType.WHATSAPP -> {
                    WhatsAppCard(result = result, onDismiss = onDismiss)
                }
                QrContentType.WIFI -> {
                    WifiCard(result = result, onDismiss = onDismiss)
                }
                QrContentType.CONTACT -> {
                    ContactCard(result = result, onDismiss = onDismiss)
                }
                else -> {
                    DefaultContentCard(result = result, onDismiss = onDismiss)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Action Row (Copy & Share)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = {
                        copyToClipboard(context, result.rawValue)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salin Payload", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = {
                        shareText(context, result.rawValue)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Specialized Smart UI Cards
// -------------------------------------------------------------

@Composable
private fun QrisCard(result: ParsedQrResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val qris = result.qrisDetails

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = qris?.merchantName ?: result.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            if (!qris?.merchantCity.isNullOrBlank()) {
                Text(
                    text = "Lokasi: ${qris?.merchantCity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (!qris?.formattedAmount.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NOMINAL PEMBAYARAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = qris?.formattedAmount ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.primary
                        )
                    }
                }
            }

            if (!qris?.acquirers.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Dukungan: " + qris?.acquirers?.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.outline
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary QRIS Action Button
    Button(
        onClick = {
            SmartActionHandler.openPaymentApp(context, result.rawValue)
            onDismiss()
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Buka Aplikasi Pembayaran",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WhatsAppCard(result: ParsedQrResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val waGreen = Color(0xFF25D366)
    val waBusinessColor = Color(0xFF0F7D63)

    val installedPackages = remember { SmartActionHandler.getInstalledWhatsAppPackages(context) }
    val hasBothWhatsApp = installedPackages.size > 1

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = waGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            if (!result.subtitle.isNullOrBlank() && result.subtitle != "Kirim pesan langsung via WhatsApp") {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (!result.whatsappMessage.isNullOrBlank()) "PESAN OTOMATIS" else "DETAIL TAUTAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.outline,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Prominent WhatsApp Action Buttons
    if (hasBothWhatsApp) {
        // Both Regular WhatsApp and WhatsApp Business installed
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    SmartActionHandler.openWhatsApp(
                        context = context,
                        rawPhone = result.whatsappPhone,
                        message = result.whatsappMessage,
                        actionUrl = result.actionUrl,
                        targetPackage = WhatsAppPackage.REGULAR.packageName
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = waGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WhatsApp",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Button(
                onClick = {
                    SmartActionHandler.openWhatsApp(
                        context = context,
                        rawPhone = result.whatsappPhone,
                        message = result.whatsappMessage,
                        actionUrl = result.actionUrl,
                        targetPackage = WhatsAppPackage.BUSINESS.packageName
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = waBusinessColor,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WA Business",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    } else {
        // Single app or fallback
        val targetPkg = installedPackages.firstOrNull()
        val btnLabel = when (targetPkg) {
            WhatsAppPackage.REGULAR -> "Buka di WhatsApp"
            WhatsAppPackage.BUSINESS -> "Buka di WhatsApp Business"
            null -> "Buka Tautan WhatsApp"
        }
        val btnColor = if (targetPkg == WhatsAppPackage.BUSINESS) waBusinessColor else waGreen

        Button(
            onClick = {
                SmartActionHandler.openWhatsApp(
                    context = context,
                    rawPhone = result.whatsappPhone,
                    message = result.whatsappMessage,
                    actionUrl = result.actionUrl,
                    targetPackage = targetPkg?.packageName
                )
                onDismiss()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = btnColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = if (targetPkg == WhatsAppPackage.BUSINESS) Icons.Default.Store else Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = btnLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WifiCard(result: ParsedQrResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var isPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = result.wifiSsid ?: "Jaringan Wi-Fi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Protokol: ${result.wifiEncryption ?: "WPA/WPA2"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!result.wifiPassword.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KATA SANDI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.outline,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = if (isPasswordVisible) result.wifiPassword else "••••••••••••",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                        }
                        FilledTonalButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isPasswordVisible) "Sembunyikan" else "Lihat", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary Auto Connect Button
    Button(
        onClick = {
            WifiConnectionHelper.connect(
                context = context,
                ssid = result.wifiSsid ?: "",
                password = result.wifiPassword,
                encryptionType = result.wifiEncryption
            )
            onDismiss()
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WifiFind,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Hubungkan ke Wi-Fi Secara Otomatis",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ContactCard(result: ParsedQrResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.contactName?.take(1)?.uppercase() ?: "K",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = result.contactName ?: "Kontak Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    if (!result.contactOrg.isNullOrBlank() || !result.contactTitle.isNullOrBlank()) {
                        val roleStr = listOfNotNull(result.contactTitle, result.contactOrg).joinToString(" di ")
                        Text(
                            text = roleStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!result.contactPhone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.contactPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            }

            if (!result.contactEmail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.contactEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary Save Contact Button
    Button(
        onClick = {
            SmartActionHandler.saveContact(
                context = context,
                name = result.contactName,
                phone = result.contactPhone,
                email = result.contactEmail,
                company = result.contactOrg,
                jobTitle = result.contactTitle,
                address = result.contactAddress
            )
            onDismiss()
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Simpan ke Kontak HP",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }

    // Quick Call / WhatsApp / Email actions
    if (!result.contactPhone.isNullOrBlank() || !result.contactEmail.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!result.contactPhone.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        SmartActionHandler.makePhoneCall(context, result.contactPhone)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Panggil")
                }

                OutlinedButton(
                    onClick = {
                        SmartActionHandler.openWhatsApp(context, result.contactPhone)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }

            if (!result.contactEmail.isNullOrBlank() && result.contactPhone.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        SmartActionHandler.sendEmail(context, result.contactEmail)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim Email")
                }
            }
        }
    }
}

@Composable
private fun DefaultContentCard(result: ParsedQrResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectionContainer {
            Text(
                text = result.rawValue,
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (result.actionUrl != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                openAction(context, result.actionUrl)
                onDismiss()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = if (result.type == QrContentType.URL) Icons.Default.OpenInBrowser else Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (result.type == QrContentType.URL) "Buka di Browser" else "Jalankan Aksi",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------------------------------------------------
// Meta & Utility Helpers
// -------------------------------------------------------------

private fun getCategoryMeta(type: QrContentType): Triple<ImageVector, String, Color?> {
    return when (type) {
        QrContentType.QRIS -> Triple(Icons.Default.AccountBalanceWallet, "Pembayaran QRIS", Color(0xFFD32F2F))
        QrContentType.WHATSAPP -> Triple(Icons.Default.Chat, "WhatsApp", Color(0xFF25D366))
        QrContentType.CONTACT -> Triple(Icons.Default.Person, "Kontak", null)
        QrContentType.URL -> Triple(Icons.Default.Language, "Tautan Web", null)
        QrContentType.WIFI -> Triple(Icons.Default.Wifi, "Jaringan Wi-Fi", null)
        QrContentType.EMAIL -> Triple(Icons.Default.Email, "Alamat Email", null)
        QrContentType.PHONE -> Triple(Icons.Default.Phone, "Nomor Telepon", null)
        QrContentType.SMS -> Triple(Icons.Default.Sms, "Pesan SMS", null)
        QrContentType.GEO -> Triple(Icons.Default.LocationOn, "Koordinat Peta", null)
        else -> Triple(Icons.Default.TextFields, "Teks / Data", null)
    }
}

private fun openAction(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka tautan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Scanned QR", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Disalin ke papan klip", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan QR").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(shareIntent)
}
