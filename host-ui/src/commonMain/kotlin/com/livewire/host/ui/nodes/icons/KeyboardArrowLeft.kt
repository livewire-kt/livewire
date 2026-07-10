package com.livewire.host.ui.nodes.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HostIcons.KeyboardArrowLeft: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "KeyboardArrowLeft",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveToRelative(432f, 480f)
      lineToRelative(156f, 156f)
      quadToRelative(11f, 11f, 11f, 28f)
      reflectiveQuadToRelative(-11f, 28f)
      quadToRelative(-11f, 11f, -28f, 11f)
      reflectiveQuadToRelative(-28f, -11f)
      lineTo(348f, 508f)
      quadToRelative(-6f, -6f, -8.5f, -13f)
      reflectiveQuadToRelative(-2.5f, -15f)
      quadToRelative(0f, -8f, 2.5f, -15f)
      reflectiveQuadToRelative(8.5f, -13f)
      lineToRelative(184f, -184f)
      quadToRelative(11f, -11f, 28f, -11f)
      reflectiveQuadToRelative(28f, 11f)
      quadToRelative(11f, 11f, 11f, 28f)
      reflectiveQuadToRelative(-11f, 28f)
      lineTo(432f, 480f)
      close()
    }
  }.build()
}
