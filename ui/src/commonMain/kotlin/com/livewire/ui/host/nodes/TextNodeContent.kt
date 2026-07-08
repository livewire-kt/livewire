package com.livewire.ui.host.nodes

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.TextNode
import com.livewire.ui.widget.TextStyle as LivewireTextStyle

@Composable
internal fun TextNodeContent(
  node: TextNode,
  modifier: Modifier = Modifier,
) {
  Text(
    text = node.text,
    color = node.color,
    style = node.style.asComposeTextStyle,
    fontWeight = node.fontWeight?.let { FontWeight(it) },
    modifier = modifier.debugFrame(),
  )
}

internal val LivewireTextStyle?.asComposeTextStyle: TextStyle
  @Composable get() = when (this) {
    LivewireTextStyle.DisplayLarge -> MaterialTheme.typography.displayLarge
    LivewireTextStyle.DisplayMedium -> MaterialTheme.typography.displayMedium
    LivewireTextStyle.DisplaySmall -> MaterialTheme.typography.displaySmall
    LivewireTextStyle.HeadlineLarge -> MaterialTheme.typography.headlineLarge
    LivewireTextStyle.HeadlineMedium -> MaterialTheme.typography.headlineMedium
    LivewireTextStyle.HeadlineSmall -> MaterialTheme.typography.headlineSmall
    LivewireTextStyle.TitleLarge -> MaterialTheme.typography.titleLarge
    LivewireTextStyle.TitleMedium -> MaterialTheme.typography.titleMedium
    LivewireTextStyle.TitleSmall -> MaterialTheme.typography.titleSmall
    LivewireTextStyle.BodyLarge -> MaterialTheme.typography.bodyLarge
    LivewireTextStyle.BodyMedium -> MaterialTheme.typography.bodyMedium
    LivewireTextStyle.BodySmall -> MaterialTheme.typography.bodySmall
    LivewireTextStyle.LabelLarge -> MaterialTheme.typography.labelLarge
    LivewireTextStyle.LabelMedium -> MaterialTheme.typography.labelMedium
    LivewireTextStyle.LabelSmall -> MaterialTheme.typography.labelSmall
    null -> LocalTextStyle.current
  }
