package com.company.cavitrack.presentation.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.ListCard
import com.company.cavitrack.presentation.components.LoadingState
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadData() })
        is UiState.Success -> {
            val data = state.data
            val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(title = "Total Components", value = data.totalComponents.toString(), modifier = Modifier.weight(1f))
                        SummaryCard(
                            title = "Low Stock", 
                            value = data.lowStockCount.toString(), 
                            modifier = Modifier.weight(1f),
                            isWarning = data.lowStockCount > 0
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(title = "Total Customers", value = data.totalCustomers.toString(), modifier = Modifier.weight(1f))
                        SummaryCard(title = "Active Molds", value = data.activeMolds.toString(), modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(data.recentActivity, key = { it.id }) { log ->
                    ListCard(onClick = { onNavigateToDetail(log.entityType, log.entityId) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = log.entityName, fontWeight = FontWeight.Bold)
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${log.action} via ${log.changeSource}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val warningBg = if (isDark) WarningDark.copy(alpha = 0.2f) else WarningLight.copy(alpha = 0.2f)
    val warningText = if (isDark) WarningDark else WarningLight

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) warningBg else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWarning) warningText else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = if (isWarning) warningText else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
