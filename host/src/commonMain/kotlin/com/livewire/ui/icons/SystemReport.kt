package com.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SystemReport: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "SystemReport",
    defaultWidth = 48.dp,
    defaultHeight = 48.dp,
    viewportWidth = 48f,
    viewportHeight = 48f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(28f, 41f)
      verticalLineToRelative(-3f)
      horizontalLineToRelative(-8f)
      verticalLineToRelative(3f)
      horizontalLineToRelative(-3f)
      curveToRelative(-1.657f, 0f, -3f, 1.343f, -3f, 3f)
      verticalLineToRelative(1f)
      horizontalLineToRelative(20f)
      verticalLineToRelative(-1f)
      curveToRelative(0f, -1.657f, -1.343f, -3f, -3f, -3f)
      horizontalLineTo(28f)
      close()
      moveTo(39f, 35f)
      horizontalLineTo(4f)
      verticalLineTo(12f)
      curveToRelative(0f, -2.761f, 2.239f, -5f, 5f, -5f)
      horizontalLineToRelative(35f)
      verticalLineToRelative(23f)
      curveTo(44f, 32.761f, 41.761f, 35f, 39f, 35f)
      close()
      moveTo(24f, 26f)
      curveToRelative(-1.381f, 0f, -2.5f, 1.119f, -2.5f, 2.5f)
      reflectiveCurveTo(22.619f, 31f, 24f, 31f)
      reflectiveCurveToRelative(2.5f, -1.119f, 2.5f, -2.5f)
      reflectiveCurveTo(25.381f, 26f, 24f, 26f)
      close()
      moveTo(26f, 11f)
      horizontalLineToRelative(-4f)
      verticalLineToRelative(9.44f)
      curveToRelative(0f, 1.657f, 1.343f, 3f, 3f, 3f)
      horizontalLineToRelative(1f)
      verticalLineTo(11f)
      close()
    }
  }.build()
}
