package com.r0adkll.livewire.ui.host.nodes.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HostIcons.FirstPage: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "FirstPage",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(251.5f, 708.5f)
      quadTo(240f, 697f, 240f, 680f)
      verticalLineToRelative(-400f)
      quadToRelative(0f, -17f, 11.5f, -28.5f)
      reflectiveQuadTo(280f, 240f)
      quadToRelative(17f, 0f, 28.5f, 11.5f)
      reflectiveQuadTo(320f, 280f)
      verticalLineToRelative(400f)
      quadToRelative(0f, 17f, -11.5f, 28.5f)
      reflectiveQuadTo(280f, 720f)
      quadToRelative(-17f, 0f, -28.5f, -11.5f)
      close()
      moveTo(552f, 480f)
      lineToRelative(156f, 156f)
      quadToRelative(11f, 11f, 11f, 28f)
      reflectiveQuadToRelative(-11f, 28f)
      quadToRelative(-11f, 11f, -28f, 11f)
      reflectiveQuadToRelative(-28f, -11f)
      lineTo(468f, 508f)
      quadToRelative(-6f, -6f, -8.5f, -13f)
      reflectiveQuadToRelative(-2.5f, -15f)
      quadToRelative(0f, -8f, 2.5f, -15f)
      reflectiveQuadToRelative(8.5f, -13f)
      lineToRelative(184f, -184f)
      quadToRelative(11f, -11f, 28f, -11f)
      reflectiveQuadToRelative(28f, 11f)
      quadToRelative(11f, 11f, 11f, 28f)
      reflectiveQuadToRelative(-11f, 28f)
      lineTo(552f, 480f)
      close()
    }
  }.build()
}
