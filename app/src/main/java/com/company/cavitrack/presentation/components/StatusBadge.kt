package com.company.cavitrack.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.company.cavitrack.presentation.theme.*

@Composable
fun StatusBadge(
    text: String,
    statusType: StatusType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (statusType) {
        StatusType.WARNING -> WarningLight.copy(alpha = 0.2f) to WarningLight
        StatusType.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        StatusType.SUCCESS -> Color.Transparent to SuccessLight
        StatusType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

enum class StatusType {
    WARNING, ERROR, SUCCESS, NEUTRAL
}
