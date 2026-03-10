package com.r0adkll.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronDown: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "ChevronDown",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 960f,
    viewportHeight = 960f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(480f, 615.38f)
      quadTo(471.31f, 615.38f, 463.37f, 612.31f)
      quadTo(455.42f, 609.23f, 448.46f, 602.27f)
      lineTo(225.85f, 379.65f)
      quadTo(216.39f, 370.19f, 216.77f, 355.5f)
      quadTo(217.16f, 340.81f, 226.62f, 331.35f)
      quadTo(236.08f, 321.88f, 250.38f, 321.88f)
      quadTo(264.69f, 321.88f, 274.15f, 331.35f)
      lineTo(480f, 537.19f)
      lineTo(686.62f, 330.58f)
      quadTo(696.08f, 321.12f, 710.38f, 321.5f)
      quadTo(724.69f, 321.88f, 734.15f, 331.35f)
      quadTo(743.62f, 340.81f, 743.62f, 355.12f)
      quadTo(743.62f, 369.42f, 734.15f, 378.88f)
      lineTo(511.54f, 602.27f)
      quadTo(504.58f, 609.23f, 496.63f, 612.31f)
      quadTo(488.69f, 615.38f, 480f, 615.38f)
      close()
    }
  }.build()
}
