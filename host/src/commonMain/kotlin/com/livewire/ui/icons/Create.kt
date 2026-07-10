package com.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Create: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "Create",
    defaultWidth = 48.dp,
    defaultHeight = 48.dp,
    viewportWidth = 48f,
    viewportHeight = 48f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(43.152f, 1f)
      curveTo(42.168f, 1f, 41.183f, 1.375f, 40.432f, 2.127f)
      lineTo(38.344f, 4.217f)
      lineTo(43.783f, 9.656f)
      lineTo(45.873f, 7.568f)
      curveTo(47.376f, 6.065f, 47.376f, 3.629f, 45.873f, 2.127f)
      curveTo(45.122f, 1.375f, 44.137f, 1f, 43.152f, 1f)
      close()
      moveTo(36.219f, 6.34f)
      lineTo(26.439f, 16.121f)
      lineTo(31.879f, 21.561f)
      lineTo(41.658f, 11.779f)
      lineTo(36.219f, 6.34f)
      close()
      moveTo(11f, 8f)
      curveTo(7.69f, 8f, 5f, 10.69f, 5f, 14f)
      lineTo(5f, 43f)
      lineTo(34f, 43f)
      curveTo(37.31f, 43f, 40f, 40.31f, 40f, 37f)
      lineTo(40f, 17.689f)
      lineTo(31.381f, 26.32f)
      lineTo(17.51f, 30.48f)
      lineTo(21.699f, 16.631f)
      lineTo(30.32f, 8f)
      lineTo(11f, 8f)
      close()
      moveTo(24.34f, 18.26f)
      lineTo(22f, 26f)
      lineTo(29.75f, 23.67f)
      lineTo(24.34f, 18.26f)
      close()
    }
  }.build()
}
