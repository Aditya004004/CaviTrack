package com.company.cavitrack.presentation.home

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.R
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.ListCard
import com.company.cavitrack.presentation.components.LoadingState
import com.company.cavitrack.presentation.components.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val isNotificationDismissed by viewModel.isNotificationDismissed.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Atmospheric radial gradient in the top-right corner
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF818CF8).copy(alpha = if (isDark) 0.08f else 0.16f),
                        Color(0xFFC7D2FE).copy(alpha = if (isDark) 0.03f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.12f),
                    radius = size.width * 0.48f
                )
            )
        }

        when (val state = uiState) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.loadData() }
            )
            is UiState.Success -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item {
                        // Greeting Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_greeting_hello),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.home_greeting_welcome),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_greeting_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        // Dashboard Section Title & Refresh Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.title_dashboard),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.home_dashboard_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Surface(
                                onClick = { viewModel.loadData() },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.cd_refresh_dashboard),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Android 13+ Notification Permission Prompt Banner
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !hasNotificationPermission && !isNotificationDismissed
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.notification_permission_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.notification_permission_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(onClick = { viewModel.dismissNotificationPrompt() }) {
                                            Text(stringResource(R.string.btn_dismiss))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        }) {
                                            Text(stringResource(R.string.btn_enable))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 2x2 Metric Cards Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DashboardMetricCard(
                                title = stringResource(R.string.label_total_components),
                                value = data.totalComponents.toString(),
                                subtitle = stringResource(R.string.home_desc_components),
                                icon = Icons.Outlined.Inventory2,
                                cardBg = if (isDark) Color(0xFF132035) else Color(0xFFF0F6FE),
                                badgeBg = if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
                                iconColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                waveColor = if (isDark) Color(0xFF1D4ED8).copy(alpha = 0.25f) else Color(0xFFBFDBFE).copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardMetricCard(
                                title = stringResource(R.string.label_low_stock),
                                value = data.lowStockCount.toString(),
                                subtitle = stringResource(R.string.home_desc_low_stock),
                                icon = Icons.Outlined.LocalShipping,
                                cardBg = if (isDark) Color(0xFF331818) else Color(0xFFFFF1EE),
                                badgeBg = if (isDark) Color(0xFF5C1D1D) else Color(0xFFFFDFD8),
                                iconColor = if (isDark) Color(0xFFF87171) else Color(0xFFEA580C),
                                waveColor = if (isDark) Color(0xFF991B1B).copy(alpha = 0.25f) else Color(0xFFFED7AA).copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DashboardMetricCard(
                                title = stringResource(R.string.label_total_customers),
                                value = data.totalCustomers.toString(),
                                subtitle = stringResource(R.string.home_desc_customers),
                                icon = Icons.Outlined.People,
                                cardBg = if (isDark) Color(0xFF0E281E) else Color(0xFFF0FDF4),
                                badgeBg = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7),
                                iconColor = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A),
                                waveColor = if (isDark) Color(0xFF059669).copy(alpha = 0.25f) else Color(0xFFBBF7D0).copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardMetricCard(
                                title = stringResource(R.string.label_active_molds),
                                value = data.activeMolds.toString(),
                                subtitle = stringResource(R.string.home_desc_molds),
                                icon = Icons.Outlined.Settings,
                                cardBg = if (isDark) Color(0xFF261838) else Color(0xFFFAF5FF),
                                badgeBg = if (isDark) Color(0xFF4C1D74) else Color(0xFFF3E8FF),
                                iconColor = if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
                                waveColor = if (isDark) Color(0xFF7E22CE).copy(alpha = 0.25f) else Color(0xFFE9D5FF).copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Recent Activity Section Header
                        Spacer(modifier = Modifier.height(26.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.label_recent_activity),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.home_recent_activity_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (data.recentActivity.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 36.dp, horizontal = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    RecentActivityEmptyIllustration()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.msg_no_recent_activity),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.home_no_activity_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(data.recentActivity, key = { it.id }) { log ->
                            ListCard(
                                onClick = { onNavigateToDetail(log.entityType.name, log.entityId) },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.entityName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = dateFormat.format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(
                                            R.string.history_log_action_format,
                                            log.action,
                                            log.changeSource
                                        ),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (log.beforeValue != null && log.afterValue != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.history_log_value_format,
                                                log.beforeValue,
                                                log.afterValue
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    cardBg: Color,
    badgeBg: Color,
    iconColor: Color,
    waveColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .drawBehind {
                val w = size.width
                val h = size.height
                val wavePath = Path().apply {
                    moveTo(w * 0.45f, h)
                    cubicTo(
                        w * 0.62f, h * 0.85f,
                        w * 0.78f, h * 0.90f,
                        w, h * 0.60f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(wavePath, color = waveColor)

                val wavePath2 = Path().apply {
                    moveTo(w * 0.65f, h)
                    cubicTo(
                        w * 0.78f, h * 0.75f,
                        w * 0.88f, h * 0.82f,
                        w, h * 0.45f
                    )
                    lineTo(w, h)
                    close()
                }
                drawPath(wavePath2, color = waveColor.copy(alpha = waveColor.alpha * 0.6f))
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(badgeBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentActivityEmptyIllustration() {
    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient decorative background dots
        Box(
            modifier = Modifier
                .size(6.dp)
                .offset(x = (-24).dp, y = (-14).dp)
                .background(Color(0xFFE2E8F0), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .offset(x = 24.dp, y = (-18).dp)
                .background(Color(0xFFE2E8F0), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(4.dp)
                .offset(x = (-18).dp, y = 20.dp)
                .background(Color(0xFFE2E8F0), CircleShape)
        )

        // Document sheet base
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(width = 46.dp, height = 54.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(3.dp)
                        .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(3.dp)
                        .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                )
            }
        }

        // Clock badge overlay
        Surface(
            shape = CircleShape,
            color = Color(0xFF3B82F6),
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
