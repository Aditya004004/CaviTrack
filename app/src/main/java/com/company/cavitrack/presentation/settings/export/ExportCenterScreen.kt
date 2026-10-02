package com.company.cavitrack.presentation.settings.export

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.domain.export.DataSetType
import com.company.cavitrack.domain.export.ExportFilterPreset
import com.company.cavitrack.domain.export.ExportFormat
import com.company.cavitrack.domain.export.ExportMetadataRecord
import com.company.cavitrack.domain.export.ExportProgressState
import com.company.cavitrack.domain.export.ScheduledExportConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCenterScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportCenterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val savedPresets by viewModel.savedPresets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Center & Reporting", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. TOP SEGMENTED TAB BAR
            ExportTabBar(
                selectedTabIndex = uiState.selectedTabIndex,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            // 2. PROGRESS / ALERT BANNER
            AnimatedVisibility(visible = uiState.progressState !is ExportProgressState.Idle) {
                when (val progress = uiState.progressState) {
                    is ExportProgressState.Processing -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(progress.currentStep, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress.progressPercentage },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    is ExportProgressState.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Export Generated!", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                        Text(progress.metadata.fileName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                    }
                                }
                                Row {
                                    IconButton(onClick = { viewModel.shareRecord(progress.metadata) }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF1B5E20))
                                    }
                                    TextButton(onClick = { viewModel.dismissProgressState() }) {
                                        Text("Dismiss", color = Color(0xFF1B5E20))
                                    }
                                }
                            }
                        }
                    }
                    is ExportProgressState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(progress.message, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { viewModel.dismissProgressState() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // 3. MAIN CONTENT AREA
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTabIndex) {
                    0 -> QuickExportTab(onQuickExport = { ds, fmt -> viewModel.executeQuickExport(ds, fmt) })
                    1 -> AdvancedExportTab(
                        uiState = uiState,
                        savedPresets = savedPresets,
                        onToggleDataSet = { viewModel.toggleDataSet(it) },
                        onSelectFormat = { viewModel.selectFormat(it) },
                        onToggleCharts = { viewModel.toggleIncludeCharts(it) },
                        onApplyPreset = { viewModel.applyPreset(it) },
                        onSavePreset = { viewModel.saveCurrentPreset(it) },
                        onClearAllDataSets = {
                            DataSetType.entries.forEach { ds ->
                                if (uiState.selectedDataSets.contains(ds) && uiState.selectedDataSets.size > 1) {
                                    viewModel.toggleDataSet(ds)
                                }
                            }
                        },
                        onOpenPreview = { viewModel.setPreviewModalOpen(true) }
                    )
                    2 -> ExportHistoryTab(
                        historyRecords = historyRecords,
                        onShareRecord = { viewModel.shareRecord(it) },
                        onDeleteRecord = { viewModel.deleteHistoryRecord(it) }
                    )
                }
            }
        }
    }

    if (uiState.isPreviewModalOpen) {
        ExportPreviewModal(
            selectedDataSets = uiState.selectedDataSets,
            selectedFormat = uiState.selectedFormat,
            preFlightSummary = uiState.preFlightSummary,
            onConfirmExport = { customName -> viewModel.executeAdvancedExport(customName) },
            onDismiss = { viewModel.setPreviewModalOpen(false) }
        )
    }
}

// ──────────────────────────────────────────────
// TOP SEGMENTED TAB BAR
// ──────────────────────────────────────────────

@Composable
private fun ExportTabBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val tabs = listOf(
            Triple(0, "Quick Export", Icons.Default.FlashOn),
            Triple(1, "Advanced", Icons.Default.Tune),
            Triple(2, "History", Icons.Default.History)
        )

        tabs.forEach { (index, label, icon) ->
            val isSelected = selectedTabIndex == index
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// CONTEXTUAL HERO BANNER
// ──────────────────────────────────────────────

@Composable
private fun ExportHeroBanner(
    tag: String,
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color = MaterialTheme.colorScheme.primary
) {
    val isDark = isSystemInDarkTheme()
    val gradientBrush = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFE8EAF6)))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = CircleShape,
                    color = badgeColor,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// TAB 1: QUICK EXPORT
// ──────────────────────────────────────────────

@Composable
private fun QuickExportTab(
    onQuickExport: (DataSetType, ExportFormat) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            ExportHeroBanner(
                tag = "QUICK EXPORT",
                title = "Instant 1-Tap Operational Exports",
                description = "Select a dataset to download using standard enterprise layout defaults.",
                icon = Icons.Default.Download,
                badgeColor = MaterialTheme.colorScheme.primary
            )
        }

        item {
            QuickExportItemCard(
                title = "Inventory & Stock Quantities",
                description = "Export all current stock levels, SKUs, and categories.",
                badgeBg = Color(0xFFE8F5E9),
                badgeIconTint = Color(0xFF2E7D32),
                icon = Icons.Default.Inventory2,
                onExportCsv = { onQuickExport(DataSetType.Inventory, ExportFormat.CSV) },
                onExportPdf = { onQuickExport(DataSetType.Inventory, ExportFormat.PDF) },
                onExportExcel = { onQuickExport(DataSetType.Inventory, ExportFormat.EXCEL) }
            )
        }

        item {
            QuickExportItemCard(
                title = "Critical Low Stock Alerts",
                description = "Filtered report of all items below minimum stock thresholds.",
                badgeBg = Color(0xFFFFEBEE),
                badgeIconTint = Color(0xFFC62828),
                icon = Icons.Default.Warning,
                onExportCsv = { onQuickExport(DataSetType.Alerts, ExportFormat.CSV) },
                onExportPdf = { onQuickExport(DataSetType.Alerts, ExportFormat.PDF) },
                onExportExcel = { onQuickExport(DataSetType.Alerts, ExportFormat.EXCEL) }
            )
        }

        item {
            QuickExportItemCard(
                title = "Mold Equipment Status",
                description = "Export active, maintenance, and retired mold fleet statuses.",
                badgeBg = Color(0xFFF3E5F5),
                badgeIconTint = Color(0xFF7B1FA2),
                icon = Icons.Default.Settings,
                onExportCsv = { onQuickExport(DataSetType.Molds, ExportFormat.CSV) },
                onExportPdf = { onQuickExport(DataSetType.Molds, ExportFormat.PDF) },
                onExportExcel = { onQuickExport(DataSetType.Molds, ExportFormat.EXCEL) }
            )
        }

        item {
            QuickExportItemCard(
                title = "History & Audit Logs",
                description = "Complete history trail of manual updates and system actions.",
                badgeBg = Color(0xFFFFF3E0),
                badgeIconTint = Color(0xFFE65100),
                icon = Icons.Default.Description,
                onExportCsv = { onQuickExport(DataSetType.HistoryLogs, ExportFormat.CSV) },
                onExportPdf = { onQuickExport(DataSetType.HistoryLogs, ExportFormat.PDF) },
                onExportExcel = { onQuickExport(DataSetType.HistoryLogs, ExportFormat.EXCEL) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickExportItemCard(
    title: String,
    description: String,
    badgeBg: Color,
    badgeIconTint: Color,
    icon: ImageVector,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeBg,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onExportPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Report", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExportExcel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel (.xlsx)", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF7B1FA2))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV", fontSize = 12.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// TAB 2: ADVANCED EXPORT
// ──────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedExportTab(
    uiState: ExportCenterUiState,
    savedPresets: List<ExportFilterPreset>,
    onToggleDataSet: (DataSetType) -> Unit,
    onSelectFormat: (ExportFormat) -> Unit,
    onToggleCharts: (Boolean) -> Unit,
    onApplyPreset: (ExportFilterPreset) -> Unit,
    onSavePreset: (String) -> Unit,
    onClearAllDataSets: () -> Unit,
    onOpenPreview: () -> Unit
) {
    var presetNameInput by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            ExportHeroBanner(
                tag = "ADVANCED EXPORT",
                title = "Customize Your Report",
                description = "Select datasets, choose format, and apply additional options.",
                icon = Icons.Default.Settings,
                badgeColor = MaterialTheme.colorScheme.primary
            )
        }

        // Saved Presets Row
        item {
            if (savedPresets.isNotEmpty()) {
                Text("Saved Filter Presets", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    savedPresets.forEach { preset ->
                        AssistChip(
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.name) },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 1. Select Datasets Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. Select Datasets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onClearAllDataSets) {
                    Text("Clear All", fontSize = 12.sp)
                }
            }

            val dataSetMeta = listOf(
                Pair(DataSetType.Inventory, Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Inventory2)),
                Pair(DataSetType.Customers, Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.People)),
                Pair(DataSetType.Molds, Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Icons.Default.Settings)),
                Pair(DataSetType.HistoryLogs, Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Description)),
                Pair(DataSetType.Alerts, Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Warning)),
                Pair(DataSetType.AnalyticsSummary, Triple(Color(0xFFE0F7FA), Color(0xFF00838F), Icons.Default.BarChart))
            )

            dataSetMeta.forEach { (ds, meta) ->
                val (badgeBg, iconTint, icon) = meta
                val isChecked = uiState.selectedDataSets.contains(ds)

                OutlinedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onToggleDataSet(ds) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleDataSet(ds) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeBg,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ds.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(ds.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Choose Export Format Section
        item {
            Text("2. Choose Export Format", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // PDF Card
                FormatCard(
                    title = "PDF",
                    subtitle = "Document",
                    badgeBg = Color(0xFFFFEBEE),
                    badgeTint = Color(0xFFC62828),
                    icon = Icons.Default.PictureAsPdf,
                    isSelected = uiState.selectedFormat == ExportFormat.PDF,
                    onSelect = { onSelectFormat(ExportFormat.PDF) },
                    modifier = Modifier.weight(1f)
                )

                // Excel Card
                FormatCard(
                    title = "Excel",
                    subtitle = "Spreadsheet",
                    badgeBg = Color(0xFFE8F5E9),
                    badgeTint = Color(0xFF2E7D32),
                    icon = Icons.Default.TableChart,
                    isSelected = uiState.selectedFormat == ExportFormat.EXCEL,
                    onSelect = { onSelectFormat(ExportFormat.EXCEL) },
                    modifier = Modifier.weight(1f)
                )

                // CSV Card
                FormatCard(
                    title = "CSV",
                    subtitle = "File",
                    badgeBg = Color(0xFFF3E5F5),
                    badgeTint = Color(0xFF7B1FA2),
                    icon = Icons.Default.Description,
                    isSelected = uiState.selectedFormat == ExportFormat.CSV,
                    onSelect = { onSelectFormat(ExportFormat.CSV) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Visual Options
        item {
            if (uiState.selectedFormat == ExportFormat.PDF) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Include Executive Visual Charts", fontWeight = FontWeight.Bold)
                        Text("Renders native vector pie and bar charts in PDF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = uiState.includeCharts, onCheckedChange = onToggleCharts)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Save Preset Input
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text("Save Filter Preset Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSavePreset(presetNameInput)
                        presetNameInput = ""
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Preset")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Action Button
        item {
            Button(
                onClick = onOpenPreview,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pre-Flight Inspection & Export (${uiState.preFlightSummary.totalRecords} records)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormatCard(
    title: String,
    subtitle: String,
    badgeBg: Color,
    badgeTint: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeBg,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = badgeTint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ──────────────────────────────────────────────
// TAB 3: EXPORT HISTORY
// ──────────────────────────────────────────────

@Composable
private fun ExportHistoryTab(
    historyRecords: List<ExportMetadataRecord>,
    onShareRecord: (ExportMetadataRecord) -> Unit,
    onDeleteRecord: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            ExportHeroBanner(
                tag = "EXPORT HISTORY",
                title = "Track Previous Exports",
                description = "View and download past reports and export activity.",
                icon = Icons.Default.CheckCircle,
                badgeColor = Color(0xFF2E7D32)
            )
        }

        // Search and Filter Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search reports...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        }

        val filteredRecords = historyRecords.filter {
            searchQuery.isBlank() || it.fileName.contains(searchQuery, ignoreCase = true)
        }

        if (filteredRecords.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No export history found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredRecords) { rec ->
                val (badgeBg, badgeTint, icon) = when (rec.format) {
                    ExportFormat.PDF -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.PictureAsPdf)
                    ExportFormat.EXCEL -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.TableChart)
                    ExportFormat.CSV -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Icons.Default.Description)
                }

                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = badgeBg,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = icon, contentDescription = null, tint = badgeTint, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(rec.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${formatDate(rec.timestamp)} • ${formatKb(rec.fileSizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text("Completed", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onShareRecord(rec) }) {
                                Icon(Icons.Default.Download, contentDescription = "Download/Share", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteRecord(rec.id) }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatKb(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024f
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024f
    return String.format("%.2f MB", mb)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}
