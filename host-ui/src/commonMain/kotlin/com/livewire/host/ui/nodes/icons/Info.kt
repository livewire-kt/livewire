package com.livewire.host.ui.nodes.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HostIcons.Info: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "Info",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(11f, 17f)
      horizontalLineTo(13f)
      verticalLineTo(11f)
      horizontalLineTo(11f)
      close()
      moveTo(12f, 2f)
      curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
      reflectiveCurveTo(6.48f, 22f, 12f, 22f)
      reflectiveCurveTo(22f, 17.52f, 22f, 12f)
      reflectiveCurveTo(17.52f, 2f, 12f, 2f)
      close()
      moveTo(12f, 20f)
      curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
      reflectiveCurveTo(7.59f, 4f, 12f, 4f)
      reflectiveCurveTo(20f, 7.59f, 20f, 12f)
      reflectiveCurveTo(16.41f, 20f, 12f, 20f)
      close()
      moveTo(11f, 9f)
      horizontalLineTo(13f)
      verticalLineTo(7f)
      horizontalLineTo(11f)
      close()
    }
  }.build()
}
