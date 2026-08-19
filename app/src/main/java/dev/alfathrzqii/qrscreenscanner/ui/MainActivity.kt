package dev.alfathrzqii.qrscreenscanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alfathrzqii.qrscreenscanner.ui.capture.ScreenCaptureActivity
import dev.alfathrzqii.qrscreenscanner.ui.history.HistoryScreen
import dev.alfathrzqii.qrscreenscanner.ui.history.HistoryViewModel
import dev.alfathrzqii.qrscreenscanner.ui.theme.AccentEmerald
import dev.alfathrzqii.qrscreenscanner.ui.theme.QrScreenScannerTheme

class MainActivity : ComponentActivity() {

    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrScreenScannerTheme {
                MainAppScreen(
                    historyViewModel = historyViewModel,
                    onLaunchScan = {
                        val intent = Intent(this, ScreenCaptureActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    historyViewModel: HistoryViewModel,
    onLaunchScan: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(colorScheme.primary, colorScheme.tertiary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "QR Screen Scanner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Pemindai QR Layar Instan",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Modern Primary Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = colorScheme.surface,
                contentColor = colorScheme.primary,
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Beranda",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Riwayat Scan",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardGuideSection(onLaunchScan = onLaunchScan)
                    1 -> HistoryScreen(viewModel = historyViewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardGuideSection(
    onLaunchScan: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedGuideMode by remember { mutableIntStateOf(0) } // 0: Bola Pintas, 1: Control Center, 2: App Shortcut

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Expressive Hero Card with Dynamic Color gradient & asymmetric corners
        Card(
            shape = RoundedCornerShape(
                topStart = 28.dp,
                bottomEnd = 28.dp,
                topEnd = 14.dp,
                bottomStart = 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerHigh
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                // Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pintasan Layar Siap Digunakan",
                            color = colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Scan QR Tanpa Simpan Gambar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Gunakan Bola Pintas Xiaomi, Control Center Tile, atau Shortcut di Home Screen untuk memindai QR langsung dari layar aplikasi lain dalam 1 ketukan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onLaunchScan,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Coba Pindai Layar Sekarang",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Step-by-step Setup Guide Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Panduan Pemasangan Pintasan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Expressive Guide Switcher Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideModeChip(
                title = "Bola Pintas",
                subtitle = "Xiaomi / POCO",
                selected = selectedGuideMode == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedGuideMode = 0 }
            )
            GuideModeChip(
                title = "Tile Control",
                subtitle = "Quick Settings",
                selected = selectedGuideMode == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedGuideMode = 1 }
            )
            GuideModeChip(
                title = "App Shortcut",
                subtitle = "Home Screen",
                selected = selectedGuideMode == 2,
                modifier = Modifier.weight(1f),
                onClick = { selectedGuideMode = 2 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedGuideMode) {
            0 -> {
                GuideStepItem(
                    stepNumber = "1",
                    icon = Icons.Default.Smartphone,
                    title = "Buka Setelan Bola Pintas",
                    description = "Masuk ke Setelan > Setelan Tambahan > Bola Pintas (Quick Ball) di HP Xiaomi/Redmi kamu."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "2",
                    icon = Icons.Default.Tune,
                    title = "Aktifkan & Pilih Pintasan",
                    description = "Nyalakan 'Hidupkan Bola Pintas', kemudian ketuk menu 'Pilih Pintasan'."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "3",
                    icon = Icons.Default.CheckCircle,
                    title = "Pilih Aplikasi 'Pindai Layar'",
                    description = "Ganti salah satu slot pintasan dengan memilih Aplikasi > 'Pindai Layar'. Sekarang kamu bisa scan langsung dari bola melayang!"
                )
            }
            1 -> {
                GuideStepItem(
                    stepNumber = "1",
                    icon = Icons.Default.NotificationsActive,
                    title = "Tarik Panel Notifikasi 2 Kali",
                    description = "Geser layar HP dari atas ke bawah sampai seluruh tombol pintasan Control Center terbuka penuh."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "2",
                    icon = Icons.Default.Tune,
                    title = "Tekan Tombol Edit Tile",
                    description = "Ketuk ikon pensil atau menu titik tiga di Control Center untuk masuk ke mode penyesuaian tombol."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "3",
                    icon = Icons.Default.CheckCircle,
                    title = "Geser 'Scan Screen QR' ke Atas",
                    description = "Temukan icon 'Scan Screen QR' pada daftar tile di bawah, lalu tahan dan geser ke panel shortcut aktif utama."
                )
            }
            2 -> {
                GuideStepItem(
                    stepNumber = "1",
                    icon = Icons.Default.TouchApp,
                    title = "Tekan Lama Ikon Aplikasi",
                    description = "Di Home Screen atau App Drawer, tahan jari kamu pada ikon aplikasi QR Screen Scanner."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "2",
                    icon = Icons.Default.Tune,
                    title = "Pilih Menu 'Pindai Layar'",
                    description = "Akan muncul popup menu dengan pilihan pintasan 'Pindai Layar' (Direct Scan)."
                )
                Spacer(modifier = Modifier.height(12.dp))
                GuideStepItem(
                    stepNumber = "3",
                    icon = Icons.Default.CheckCircle,
                    title = "Pin ke Layar Utama",
                    description = "Tahan ikon pintasan 'Pindai Layar' tersebut lalu geser dan tempatkan langsung di Home Screen kamu."
                )
            }
        }
    }
}

@Composable
fun GuideModeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerLow
    val contentColor = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface
    val borderColor = if (selected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) contentColor.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant,
                maxLines = 1,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun GuideStepItem(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    color = colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
