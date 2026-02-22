package com.r0adkll.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChatBubbleFilled: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "ChatBubbleFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveToRelative(240f, 720f)
      lineToRelative(-92f, 92f)
      quadToRelative(-19f, 19f, -43.5f, 8.5f)
      reflectiveQuadTo(80f, 783f)
      verticalLineToRelative(-623f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(160f, 80f)
      horizontalLineToRelative(640f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(880f, 160f)
      verticalLineToRelative(480f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(800f, 720f)
      lineTo(240f, 720f)
      close()
    }
  }.build()
}

val ChatBubbleOutline: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "ChatBubbleOutline",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveToRelative(240f, 720f)
      lineToRelative(-92f, 92f)
      quadToRelative(-19f, 19f, -43.5f, 8.5f)
      reflectiveQuadTo(80f, 783f)
      verticalLineToRelative(-623f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(160f, 80f)
      horizontalLineToRelative(640f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(880f, 160f)
      verticalLineToRelative(480f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(800f, 720f)
      lineTo(240f, 720f)
      close()
      moveTo(206f, 640f)
      horizontalLineToRelative(594f)
      verticalLineToRelative(-480f)
      lineTo(160f, 160f)
      verticalLineToRelative(525f)
      lineToRelative(46f, -45f)
      close()
      moveTo(160f, 640f)
      verticalLineToRelative(-480f)
      verticalLineToRelative(480f)
      close()
    }
  }.build()
}
