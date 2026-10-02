package com.company.cavitrack.presentation.history

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.company.cavitrack.R
import com.company.cavitrack.presentation.theme.*
import com.company.cavitrack.domain.model.HistoryLog
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.SkeletonList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> }
) {
    val historyLogs = viewModel.pagedHistoryLogs.collectAsLazyPagingItems()
    val selectedAction by viewModel.selectedAction.collectAsStateWithLifecycle()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Atmospheric radial gradient in top-right corner
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmbientGlowPrimary.copy(alpha = if (isDark) 0.08f else 0.16f),
                        AmbientGlowSecondary.copy(alpha = if (isDark) 0.03f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.15f),
                    radius = size.width * 0.48f
                )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Filter History Action Pill Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) SlateDark else BlueTintLight,
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFDBEAFE)),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = stringResource(R.string.label_filter_history),
                            tint = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedAction ?: stringResource(R.string.label_filter_history),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) SlateLightSurface else SlateDark
                        )
                    }
                }
            }

            // Section Subheader: Recent Activity & Record Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_recent_activity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.history_records_count, historyLogs.itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Paged Content
            if (historyLogs.loadState.refresh is LoadState.Loading) {
                SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
            } else if (historyLogs.loadState.refresh is LoadState.Error) {
                val error = (historyLogs.loadState.refresh as LoadState.Error).error
                val errorMessage = when (error) {
                    is java.io.IOException -> stringResource(R.string.error_network)
                    else -> stringResource(R.string.error_load_history)
                }
                ErrorState(message = errorMessage, onRetry = { historyLogs.retry() })
            } else if (historyLogs.itemCount == 0) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.msg_no_stock_movements),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.msg_adjust_filters),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(count = historyLogs.itemCount, key = historyLogs.itemKey { it.id }) { index ->
                        val log = historyLogs[index]
                        if (log != null) {
                            val isDeleted = log.action.equals("Deleted", ignoreCase = true)
                            HistoryLogCard(
                                log = log,
                                dateFormat = dateFormat,
                                onClick = {
                                    if (isDeleted) {
                                        Toast.makeText(context, R.string.toast_item_deleted, Toast.LENGTH_SHORT).show()
                                    } else {
                                        onNavigateToDetail(log.entityType.name, log.entityId)
                                    }
                                }
                            )
                        }
                    }

                    if (historyLogs.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (historyLogs.loadState.append is LoadState.Error) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { historyLogs.retry() }) {
                                    Text(stringResource(R.string.btn_retry))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Options Bottom Sheet
        if (showFilterSheet) {
            androidx.activity.compose.BackHandler { showFilterSheet = false }
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.title_filter_action),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val actions = listOf(null, "Created", "Updated", "Stock Adjusted", "Photo Added")
                    val labels = listOf(
                        stringResource(R.string.filter_all),
                        stringResource(R.string.filter_created),
                        stringResource(R.string.filter_updated),
                        stringResource(R.string.filter_stock_adjusted),
                        stringResource(R.string.filter_photo_added)
                    )

                    actions.forEachIndexed { index, action ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActionFilter(action) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedAction == action,
                                onClick = { viewModel.setActionFilter(action) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_apply_filters))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryLogCard(
    log: HistoryLog,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val (badgeBg, badgeTint, badgeIcon: ImageVector) = when (log.action) {
        "Stock Adjusted" -> Triple(
            if (isDark) Color(0xFF0E281E) else Color(0xFFDCFCE7),
            if (isDark) Color(0xFF34D399) else Color(0xFF16A34A),
            Icons.Outlined.Upload
        )
        "Created" -> Triple(
            if (isDark) Color(0xFF132035) else Color(0xFFDBEAFE),
            MaterialTheme.colorScheme.primary,
            Icons.Default.Add
        )
        "Photo Added" -> Triple(
            if (isDark) Color(0xFF261838) else Color(0xFFF3E8FF),
            if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
            Icons.Outlined.AddAPhoto
        )
        "Updated" -> Triple(
            if (isDark) Color(0xFF33200D) else Color(0xFFFEF3C7),
            if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
            Icons.Outlined.Edit
        )
        else -> Triple(
            if (isDark) SlateDark else SlateLightSurface,
            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            Icons.Default.History
        )
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(badgeBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center Content: Name, Action via Source, Value Diff
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.entityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.history_log_action_format,
                        log.action,
                        log.changeSource
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (log.beforeValue != null && log.afterValue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val beforeNum = log.beforeValue.toIntOrNull()
                        val afterNum = log.afterValue.toIntOrNull()

                        val (beforeColor, afterColor) = if (beforeNum != null && afterNum != null) {
                            if (afterNum < beforeNum) {
                                Pair(
                                    if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
                                    if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                                )
                            } else {
                                Pair(
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
                                )
                            }
                        } else {
                            Pair(
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = log.beforeValue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = beforeColor
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = log.afterValue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = afterColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Timestamp and More Indicator
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
