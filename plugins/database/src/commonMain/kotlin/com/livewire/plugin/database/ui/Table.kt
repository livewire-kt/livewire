package com.livewire.plugin.database.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val Icons.Table: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "Table",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(200f, 840f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(120f, 760f)
      verticalLineToRelative(-560f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(200f, 120f)
      horizontalLineToRelative(560f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(840f, 200f)
      verticalLineToRelative(560f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(760f, 840f)
      horizontalLineTo(200f)
      close()
      moveToRelative(240f, -240f)
      horizontalLineTo(200f)
      verticalLineToRelative(160f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(-160f)
      close()
      moveToRelative(80f, 0f)
      verticalLineToRelative(160f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(-160f)
      horizontalLineTo(520f)
      close()
      moveToRelative(-80f, -80f)
      verticalLineToRelative(-160f)
      horizontalLineTo(200f)
      verticalLineToRelative(160f)
      horizontalLineToRelative(240f)
      close()
      moveToRelative(80f, 0f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(-160f)
      horizontalLineTo(520f)
      verticalLineToRelative(160f)
      close()
      moveTo(200f, 280f)
      horizontalLineToRelative(560f)
      verticalLineToRelative(-80f)
      horizontalLineTo(200f)
      verticalLineToRelative(80f)
      close()
    }
  }.build()
}
