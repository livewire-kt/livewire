package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

actual fun Modifier.cursorForHorizontalResize(isHorizontal: Boolean): Modifier {
  return pointerHoverIcon(PointerIcon.Hand)
}
