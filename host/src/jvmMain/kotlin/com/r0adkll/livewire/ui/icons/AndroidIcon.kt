package com.r0adkll.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AndroidIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "android",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f
  ).apply {
    path(
      fill = SolidColor(Color(0xFF3DDC84))
    ) {
      moveTo(40f, 720f)
      quadToRelative(9f, -107f, 65.5f, -197f)
      reflectiveQuadTo(256f, 380f)
      lineToRelative(-74f, -128f)
      quadToRelative(-6f, -9f, -3f, -19f)
      reflectiveQuadToRelative(13f, -15f)
      quadToRelative(8f, -5f, 18f, -2f)
      reflectiveQuadToRelative(16f, 12f)
      lineToRelative(74f, 128f)
      quadToRelative(86f, -36f, 180f, -36f)
      reflectiveQuadToRelative(180f, 36f)
      lineToRelative(74f, -128f)
      quadToRelative(6f, -9f, 16f, -12f)
      reflectiveQuadToRelative(18f, 2f)
      quadToRelative(10f, 5f, 13f, 15f)
      reflectiveQuadToRelative(-3f, 19f)
      lineToRelative(-74f, 128f)
      quadToRelative(94f, 53f, 150.5f, 143f)
      reflectiveQuadTo(920f, 720f)
      horizontalLineTo(40f)
      close()
      moveToRelative(275.5f, -124.5f)
      quadTo(330f, 581f, 330f, 560f)
      reflectiveQuadToRelative(-14.5f, -35.5f)
      quadTo(301f, 510f, 280f, 510f)
      reflectiveQuadToRelative(-35.5f, 14.5f)
      quadTo(230f, 539f, 230f, 560f)
      reflectiveQuadToRelative(14.5f, 35.5f)
      quadTo(259f, 610f, 280f, 610f)
      reflectiveQuadToRelative(35.5f, -14.5f)
      close()
      moveToRelative(400f, 0f)
      quadTo(730f, 581f, 730f, 560f)
      reflectiveQuadToRelative(-14.5f, -35.5f)
      quadTo(701f, 510f, 680f, 510f)
      reflectiveQuadToRelative(-35.5f, 14.5f)
      quadTo(630f, 539f, 630f, 560f)
      reflectiveQuadToRelative(14.5f, 35.5f)
      quadTo(659f, 610f, 680f, 610f)
      reflectiveQuadToRelative(35.5f, -14.5f)
      close()
    }
  }.build()
}
