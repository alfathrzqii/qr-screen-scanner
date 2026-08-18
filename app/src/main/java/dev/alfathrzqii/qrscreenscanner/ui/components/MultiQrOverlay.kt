package dev.alfathrzqii.qrscreenscanner.ui.components

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun MultiQrOverlay(
    barcodes: List<Barcode>,
    onSelectQr: (Barcode) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Floating Top Banner Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceContainerHighest,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "${barcodes.size} QR Terdeteksi di Layar",
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ketuk kotak QR yang ingin kamu buka",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Bounding boxes around each QR code
        barcodes.forEachIndexed { index, barcode ->
            val boundingBox: Rect = barcode.boundingBox ?: return@forEachIndexed

            val boxWidthDp = with(density) { boundingBox.width().toDp() }
            val boxHeightDp = with(density) { boundingBox.height().toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(boundingBox.left, boundingBox.top) }
                    .size(boxWidthDp, boxHeightDp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primary.copy(alpha = 0.2f))
                    .border(2.5.dp, colorScheme.primary, RoundedCornerShape(12.dp))
                    .clickable {
                        onSelectQr(barcode)
                    },
                contentAlignment = Alignment.TopEnd
            ) {
                // Badge index
                Surface(
                    shape = CircleShape,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
