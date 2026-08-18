package com.alfath.qrscreenscanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfath.qrscreenscanner.data.local.QrContentType
import com.alfath.qrscreenscanner.data.local.ScanHistoryEntity
import com.alfath.qrscreenscanner.ui.theme.AccentCyan
import com.alfath.qrscreenscanner.ui.theme.AccentEmerald
import com.alfath.qrscreenscanner.ui.theme.AccentRose
import com.alfath.qrscreenscanner.ui.theme.DarkBorder
import com.alfath.qrscreenscanner.ui.theme.DarkSurface
import com.alfath.qrscreenscanner.ui.theme.PrimaryBlue
import com.alfath.qrscreenscanner.ui.theme.TextMuted
import com.alfath.qrscreenscanner.ui.theme.TextPrimary
import com.alfath.qrscreenscanner.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryItemCard(
    scan: ScanHistoryEntity,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tintColor) = getIconForType(scan.contentType)
    val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(scan.timestamp))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = scan.displayTitle,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = scan.rawValue,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun getIconForType(type: QrContentType): Pair<ImageVector, Color> {
    return when (type) {
        QrContentType.URL -> Pair(Icons.Default.Language, PrimaryBlue)
        QrContentType.WIFI -> Pair(Icons.Default.Wifi, AccentEmerald)
        QrContentType.EMAIL -> Pair(Icons.Default.Email, AccentCyan)
        QrContentType.PHONE -> Pair(Icons.Default.Phone, AccentEmerald)
        QrContentType.SMS -> Pair(Icons.Default.Email, AccentCyan)
        QrContentType.GEO -> Pair(Icons.Default.Language, AccentRose)
        else -> Pair(Icons.Default.TextFields, TextMuted)
    }
}
