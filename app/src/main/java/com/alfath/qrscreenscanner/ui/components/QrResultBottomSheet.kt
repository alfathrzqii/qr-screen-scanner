package com.alfath.qrscreenscanner.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.alfath.qrscreenscanner.data.local.QrContentType
import com.alfath.qrscreenscanner.ui.theme.AccentCyan
import com.alfath.qrscreenscanner.ui.theme.AccentEmerald
import com.alfath.qrscreenscanner.ui.theme.AccentRose
import com.alfath.qrscreenscanner.ui.theme.DarkBorder
import com.alfath.qrscreenscanner.ui.theme.DarkSurface
import com.alfath.qrscreenscanner.ui.theme.DarkSurfaceVariant
import com.alfath.qrscreenscanner.ui.theme.PrimaryBlue
import com.alfath.qrscreenscanner.ui.theme.TextMuted
import com.alfath.qrscreenscanner.ui.theme.TextPrimary
import com.alfath.qrscreenscanner.ui.theme.TextSecondary
import com.alfath.qrscreenscanner.util.ParsedQrResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrResultBottomSheet(
    result: ParsedQrResult,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color(0x99000000),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Type Badge & Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val (icon, tintColor, badgeText) = getIconAndBadgeForType(result.type)

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tintColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badgeText,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badgeText.uppercase(),
                        color = tintColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = result.displayTitle,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Raw Content Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.rawValue,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (result.type == QrContentType.URL && result.actionUrl != null) {
                    Button(
                        onClick = {
                            openUrl(context, result.actionUrl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka di Browser", fontWeight = FontWeight.SemiBold)
                    }
                } else if (result.actionUrl != null) {
                    Button(
                        onClick = {
                            openUrl(context, result.actionUrl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Aksi", fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            copyToClipboard(context, result.rawValue)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = {
                            shareText(context, result.rawValue)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun getIconAndBadgeForType(type: QrContentType): Triple<ImageVector, Color, String> {
    return when (type) {
        QrContentType.URL -> Triple(Icons.Default.Language, PrimaryBlue, "Tautan Web")
        QrContentType.WIFI -> Triple(Icons.Default.Wifi, AccentEmerald, "Wi-Fi Network")
        QrContentType.EMAIL -> Triple(Icons.Default.Email, AccentCyan, "Email")
        QrContentType.PHONE -> Triple(Icons.Default.Phone, AccentEmerald, "Nomor Telepon")
        QrContentType.SMS -> Triple(Icons.Default.Email, AccentCyan, "Pesan SMS")
        QrContentType.GEO -> Triple(Icons.Default.Language, AccentRose, "Lokasi Peta")
        else -> Triple(Icons.Default.TextFields, TextMuted, "Teks / Data")
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Scanned QR", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan QR Code").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(shareIntent)
}
