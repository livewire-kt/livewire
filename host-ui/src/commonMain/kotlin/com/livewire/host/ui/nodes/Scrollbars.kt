package com.livewire.host.ui.nodes

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Theme-derived scrollbar colors, hoisted to common code so scrollbars track the
 * active [MaterialTheme.colorScheme] in both light and dark modes.
 */
@Immutable
internal data class ScrollbarColors(
  val unhoverColor: Color,
  val hoverColor: Color,
)

@Composable
internal fun rememberScrollbarColors(): ScrollbarColors {
  val onSurface = MaterialTheme.colorScheme.onSurface
  return remember(onSurface) {
    ScrollbarColors(
      unhoverColor = onSurface.copy(alpha = 0.25f),
      hoverColor = onSurface.copy(alpha = 0.55f),
    )
  }
}

// Compose's scrollbar components only exist on desktop, so touch-driven
// hosts (Android, iOS) render nothing here.
@Composable
internal expect fun VerticalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier = Modifier,
  colors: ScrollbarColors = rememberScrollbarColors(),
)

@Composable
internal expect fun HorizontalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier = Modifier,
  colors: ScrollbarColors = rememberScrollbarColors(),
)
