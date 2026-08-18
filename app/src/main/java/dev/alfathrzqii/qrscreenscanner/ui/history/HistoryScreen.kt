package dev.alfathrzqii.qrscreenscanner.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import dev.alfathrzqii.qrscreenscanner.data.local.ScanHistoryEntity
import dev.alfathrzqii.qrscreenscanner.ui.components.HistoryItemCard
import dev.alfathrzqii.qrscreenscanner.ui.components.QrResultBottomSheet
import dev.alfathrzqii.qrscreenscanner.util.ParsedQrResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val historyList by viewModel.historyList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var selectedScanForResult by remember { mutableStateOf<ParsedQrResult?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Search Bar & Clear All header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        "Cari riwayat scan…",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outlineVariant,
                    focusedContainerColor = colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = colorScheme.surfaceContainerLow,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )

            if (historyList.isNotEmpty() || searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Hapus Semua",
                        tint = colorScheme.error
                    )
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { viewModel.selectFilter(null) },
                label = { Text("Semua") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primaryContainer,
                    selectedLabelColor = colorScheme.onPrimaryContainer
                )
            )

            FilterChip(
                selected = selectedFilter == QrContentType.URL,
                onClick = { viewModel.selectFilter(if (selectedFilter == QrContentType.URL) null else QrContentType.URL) },
                label = { Text("Tautan Web") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primaryContainer,
                    selectedLabelColor = colorScheme.onPrimaryContainer
                )
            )

            FilterChip(
                selected = selectedFilter == QrContentType.WIFI,
                onClick = { viewModel.selectFilter(if (selectedFilter == QrContentType.WIFI) null else QrContentType.WIFI) },
                label = { Text("Wi-Fi") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primaryContainer,
                    selectedLabelColor = colorScheme.onPrimaryContainer
                )
            )

            FilterChip(
                selected = selectedFilter == QrContentType.TEXT,
                onClick = { viewModel.selectFilter(if (selectedFilter == QrContentType.TEXT) null else QrContentType.TEXT) },
                label = { Text("Teks") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primaryContainer,
                    selectedLabelColor = colorScheme.onPrimaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // List or Empty State
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isEmpty()) Icons.Default.History else Icons.Default.Search,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (searchQuery.isEmpty()) "Belum ada riwayat scan" else "Tidak ada hasil ditemukan",
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isEmpty()) "Scan QR code di layar menggunakan shortcut tile di Control Center." else "Coba periksa kembali ejaan atau gunakan kata kunci lain.",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(historyList, key = { it.id }) { scan ->
                    HistoryItemCard(
                        scan = scan,
                        onClick = {
                            selectedScanForResult = ParsedQrResult(
                                rawValue = scan.rawValue,
                                displayTitle = scan.displayTitle,
                                type = scan.contentType,
                                actionUrl = if (scan.contentType == QrContentType.URL) scan.rawValue else null
                            )
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("QR Text", scan.rawValue))
                            Toast.makeText(context, "Disalin ke papan klip", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteScan(scan)
                        }
                    )
                }
            }
        }
    }

    // Detail BottomSheet
    selectedScanForResult?.let { result ->
        QrResultBottomSheet(
            result = result,
            onDismiss = { selectedScanForResult = null }
        )
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    "Hapus Semua Riwayat?",
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Seluruh riwayat hasil scan QR code yang tersimpan di memori lokal akan dihapus permanen.",
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        "Hapus Permanen",
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = colorScheme.onSurfaceVariant)
                }
            },
            containerColor = colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
