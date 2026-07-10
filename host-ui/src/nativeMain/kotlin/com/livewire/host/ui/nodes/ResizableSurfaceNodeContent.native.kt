package com.livewire.host.ui.nodes

import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

actual fun androidx.compose.ui.Modifier.cursorForHorizontalResize(isHorizontal: Boolean): androidx.compose.ui.Modifier {
  return pointerHoverIcon(PointerIcon.Hand)
}
