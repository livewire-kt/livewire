package com.livewire.host.ui.nodes

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.HorizontalScrollbar as DesktopHorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar as DesktopVerticalScrollbar

@Composable
internal actual fun VerticalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier,
  colors: ScrollbarColors,
) {
  DesktopVerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = colors.toScrollbarStyle(),
  )
}

@Composable
internal actual fun HorizontalScrollbar(
  scrollState: ScrollState,
  modifier: Modifier,
  colors: ScrollbarColors,
) {
  DesktopHorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = colors.toScrollbarStyle(),
  )
}

@Composable
private fun ScrollbarColors.toScrollbarStyle(): ScrollbarStyle {
  // Keep the ambient shape/thickness/hover timing; only the colors are themed.
  val base = LocalScrollbarStyle.current
  return remember(this, base) {
    base.copy(
      unhoverColor = unhoverColor,
      hoverColor = hoverColor,
    )
  }
}
