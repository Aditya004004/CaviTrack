package com.company.cavitrack.presentation.history

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
import com.company.cavitrack.presentation.components.EmptyState
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.ListCard
import com.company.cavitrack.presentation.components.LoadingState
import com.company.cavitrack.presentation.components.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadData() })
        is UiState.Success -> {
            val history = state.data
            if (history.isEmpty()) {
                EmptyState(message = "No history available")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(history, key = { it.id }) { log ->
                        ListCard(onClick = { onNavigateToDetail(log.entityType, log.entityId) }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.entityName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }.format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${log.action} via ${log.changeSource}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (log.beforeValue != null && log.afterValue != null) {
                                    Text(
                                        text = "${log.beforeValue} -> ${log.afterValue}",
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
