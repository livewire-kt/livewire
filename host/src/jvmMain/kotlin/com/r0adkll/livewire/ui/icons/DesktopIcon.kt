package com.r0adkll.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DesktopIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "desktop",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(
      fill = SolidColor(Color(0xFFE8EAED))
    ) {
      moveTo(320f, 840f)
      verticalLineToRelative(-80f)
      horizontalLineTo(160f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(80f, 680f)
      verticalLineTo(240f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(160f, 160f)
      horizontalLineToRelative(640f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(880f, 240f)
      verticalLineToRelative(440f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(800f, 760f)
      horizontalLineTo(640f)
      verticalLineToRelative(80f)
      horizontalLineTo(320f)
      close()
      moveTo(160f, 680f)
      horizontalLineToRelative(640f)
      verticalLineTo(240f)
      horizontalLineTo(160f)
      verticalLineToRelative(440f)
      close()
    }
  }.build()
}
