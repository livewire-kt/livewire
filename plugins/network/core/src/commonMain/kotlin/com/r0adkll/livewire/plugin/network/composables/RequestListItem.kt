package com.r0adkll.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.network.data.NetworkEvent
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.ProgressIndicator
import com.r0adkll.livewire.ui.widget.ProgressIndicatorStyle
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle

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
            style = TextStyle.LabelMedium,
            color = statusColor(statusCode),
          )
        } else if (event.error != null) {
          Text(
            text = "ERR",
            style = TextStyle.LabelMedium,
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
        style = TextStyle.LabelMedium,
        fontWeight = 700,
        modifier = LivewireModifier.width(60.dp),
      )

      // URL path
      Text(
        text = extractPath(event.request.url),
        style = TextStyle.BodySmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
        modifier = LivewireModifier.weight(1f),
      )

      // Duration
      if (event.durationMs != null) {
        Text(
          text = "${event.durationMs}ms",
          style = TextStyle.LabelSmall,
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

private fun extractPath(url: String): String {
  return try {
    val withoutScheme = url.substringAfter("://")
    val path = withoutScheme.substringAfter("/", missingDelimiterValue = "/")
    "/$path"
  } catch (_: Exception) {
    url
  }
}
