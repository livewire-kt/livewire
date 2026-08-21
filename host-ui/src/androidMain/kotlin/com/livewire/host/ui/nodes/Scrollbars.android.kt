package com.livewire.host.ui.nodes

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun VerticalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier,
  colors: ScrollbarColors,
) {
  // No scrollbar component on Android; content scrolls by touch.
}

@Composable
internal actual fun HorizontalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier,
  colors: ScrollbarColors,
) {
  // No scrollbar component on Android; content scrolls by touch.
}
