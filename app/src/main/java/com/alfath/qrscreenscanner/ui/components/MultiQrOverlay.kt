package com.alfath.qrscreenscanner.ui.components

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
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
import com.alfath.qrscreenscanner.ui.theme.DarkSurface
import com.alfath.qrscreenscanner.ui.theme.OverlayScrim
import com.alfath.qrscreenscanner.ui.theme.PrimaryBlue
import com.alfath.qrscreenscanner.ui.theme.QrHighlight
import com.alfath.qrscreenscanner.ui.theme.TextMuted
import com.alfath.qrscreenscanner.ui.theme.TextPrimary
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun MultiQrOverlay(
    barcodes: List<Barcode>,
    onSelectQr: (Barcode) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayScrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Top instruction pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "${barcodes.size} QR Code Terdeteksi",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ketuk kotak QR yang ingin kamu buka",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Bounding boxes for each QR
        barcodes.forEachIndexed { index, barcode ->
            val boundingBox: Rect = barcode.boundingBox ?: return@forEachIndexed

            val boxWidthDp = with(density) { boundingBox.width().toDp() }
            val boxHeightDp = with(density) { boundingBox.height().toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(boundingBox.left, boundingBox.top) }
                    .size(boxWidthDp, boxHeightDp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3338BDF8))
                    .border(2.5.dp, QrHighlight, RoundedCornerShape(8.dp))
                    .clickable {
                        onSelectQr(barcode)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Badge number
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QR #${index + 1}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
