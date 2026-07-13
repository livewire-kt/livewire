package com.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.livewire.plugin.network.data.NetworkEvent
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.ProgressIndicator
import com.livewire.ui.widget.ProgressIndicatorStyle
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text

@Composable
internal fun RequestListItem(
  event: NetworkEvent,
  isSelected: Boolean,
  onClick: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
) {
  Surface(
    onClick = onClick,
    color = if (isSelected) LivewireTheme.colorScheme.surfaceContainerHighest else null,
    shape = RoundedCornerShape(8.dp),
    modifier = modifier
      .padding(
        horizontal = 8.dp,
        vertical = 2.dp,
      )
      .fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
      // Timestamp
      Text(
        text = formatTimestamp(event.request.timestamp),
        style = LivewireTheme.typography.labelSmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
        modifier = LivewireModifier.padding(right = 8.dp),
      )

      Box(
        contentAlignment = Alignment.Center,
        modifier = LivewireModifier
          .size(40.dp),
      ) {
        // Status code or in-flight indicator
        if (event.response != null) {
          val statusCode = event.response.statusCode
          Text(
            text = statusCode.toString(),
            style = LivewireTheme.typography.labelMedium,
            color = statusColor(statusCode),
          )
        } else if (event.error != null) {
          Text(
            text = "ERR",
            style = LivewireTheme.typography.labelMedium,
            color = Color.Red,
          )
        } else {
          ProgressIndicator(
            style = ProgressIndicatorStyle.Circular,
            modifier = LivewireModifier.size(24.dp),
          )
        }
      }

      // Method
      Text(
        text = event.request.method,
        style = LivewireTheme.typography.labelMedium,
        fontWeight = 700,
        modifier = LivewireModifier.width(60.dp),
      )

      // URL path
      Text(
        text = extractPath(event.request.url),
        style = LivewireTheme.typography.bodySmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
        modifier = LivewireModifier.weight(1f),
      )

      // Duration
      if (event.durationMs != null) {
        Text(
          text = "${event.durationMs}ms",
          style = LivewireTheme.typography.labelSmall,
          color = LivewireTheme.colorScheme.onSurfaceVariant,
          modifier = LivewireModifier.padding(left = 8.dp),
        )
      }

    }
  }
}

private fun statusColor(code: Int): Color = when {
  code in 200..299 -> Color(0xFF4CAF50)
  code in 300..399 -> Color(0xFFFF9800)
  code in 400..499 -> Color(0xFFFF5722)
  code >= 500 -> Color(0xFFF44336)
  else -> Color.Gray
}

private fun formatTimestamp(epochMillis: Long): String {
  val totalSeconds = epochMillis / 1000
  val millis = epochMillis % 1000
  val seconds = totalSeconds % 60
  val minutes = (totalSeconds / 60) % 60
  val hours = (totalSeconds / 3600) % 24
  return "${hours.toString().padStart(2, '0')}:" +
    "${minutes.toString().padStart(2, '0')}:" +
    "${seconds.toString().padStart(2, '0')}." +
    millis.toString().padStart(3, '0')
}

private fun extractPath(url: String): String {
  return try {
    val withoutScheme = url.substringAfter("://")
    val path = withoutScheme.substringAfter("/", missingDelimiterValue = "/")
    "/$path"
  } catch (_: Exception) {
    url
  }
}
