package com.livewire.plugin.database.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val Icons.Database: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "Database",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(735f, 393f)
      quadToRelative(105f, -47f, 105f, -113f)
      reflectiveQuadTo(735f, 167f)
      quadToRelative(-105f, -47f, -255f, -47f)
      reflectiveQuadToRelative(-255f, 47f)
      quadToRelative(-105f, 47f, -105f, 113f)
      reflectiveQuadToRelative(105f, 113f)
      quadToRelative(105f, 47f, 255f, 47f)
      reflectiveQuadToRelative(255f, -47f)
      close()
      moveTo(582.5f, 531.5f)
      quadTo(644f, 523f, 701f, 504f)
      reflectiveQuadToRelative(98f, -49.5f)
      quadToRelative(41f, -30.5f, 41f, -74.5f)
      verticalLineToRelative(100f)
      quadToRelative(0f, 44f, -41f, 74.5f)
      reflectiveQuadTo(701f, 604f)
      quadToRelative(-57f, 19f, -118.5f, 27.5f)
      reflectiveQuadTo(480f, 640f)
      quadToRelative(-41f, 0f, -102.5f, -8.5f)
      reflectiveQuadTo(259f, 604f)
      quadToRelative(-57f, -19f, -98f, -49.5f)
      reflectiveQuadTo(120f, 480f)
      verticalLineToRelative(-100f)
      quadToRelative(0f, 44f, 41f, 74.5f)
      reflectiveQuadToRelative(98f, 49.5f)
      quadToRelative(57f, 19f, 118.5f, 27.5f)
      reflectiveQuadTo(480f, 540f)
      quadToRelative(41f, 0f, 102.5f, -8.5f)
      close()
      moveToRelative(0f, 200f)
      quadTo(644f, 723f, 701f, 704f)
      reflectiveQuadToRelative(98f, -49.5f)
      quadToRelative(41f, -30.5f, 41f, -74.5f)
      verticalLineToRelative(100f)
      quadToRelative(0f, 44f, -41f, 74.5f)
      reflectiveQuadTo(701f, 804f)
      quadToRelative(-57f, 19f, -118.5f, 27.5f)
      reflectiveQuadTo(480f, 840f)
      quadToRelative(-41f, 0f, -102.5f, -8.5f)
      reflectiveQuadTo(259f, 804f)
      quadToRelative(-57f, -19f, -98f, -49.5f)
      reflectiveQuadTo(120f, 680f)
      verticalLineToRelative(-100f)
      quadToRelative(0f, 44f, 41f, 74.5f)
      reflectiveQuadToRelative(98f, 49.5f)
      quadToRelative(57f, 19f, 118.5f, 27.5f)
      reflectiveQuadTo(480f, 740f)
      quadToRelative(41f, 0f, 102.5f, -8.5f)
      close()
    }
  }.build()
}
