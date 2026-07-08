package com.livewire.plugin.network.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val Icons.Network: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "Network",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(14.891f, 2.496f)
      curveTo(14.508f, 2.423f, 14.101f, 2.579f, 13.871f, 2.924f)
      curveTo(13.564f, 3.384f, 13.688f, 4.005f, 14.148f, 4.311f)
      lineTo(16.697f, 6f)
      lineTo(14f, 6f)
      curveTo(13.448f, 6f, 13f, 6.448f, 13f, 7f)
      lineTo(13f, 8f)
      lineTo(8f, 8f)
      curveTo(5.25f, 8f, 3f, 10.25f, 3f, 13f)
      lineTo(3f, 14f)
      lineTo(3.203f, 14f)
      curveTo(3.683f, 16.258f, 5.605f, 18f, 8f, 18f)
      lineTo(12f, 18f)
      curveTo(12.565f, 18f, 13f, 18.435f, 13f, 19f)
      lineTo(13f, 21f)
      arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = false, 15f, 21f)
      lineTo(15f, 19f)
      curveTo(15f, 17.355f, 13.645f, 16f, 12f, 16f)
      lineTo(8f, 16f)
      curveTo(6.332f, 16f, 5f, 14.668f, 5f, 13f)
      curveTo(5f, 11.332f, 6.332f, 10f, 8f, 10f)
      lineTo(13f, 10f)
      lineTo(13f, 11f)
      curveTo(13f, 11.552f, 13.448f, 12f, 14f, 12f)
      lineTo(20f, 12f)
      curveTo(20.552f, 12f, 21f, 11.552f, 21f, 11f)
      lineTo(21f, 7f)
      curveTo(21f, 6.666f, 20.833f, 6.353f, 20.555f, 6.168f)
      lineTo(15.258f, 2.646f)
      curveTo(15.143f, 2.57f, 15.018f, 2.52f, 14.891f, 2.496f)
      close()
    }
  }.build()
}
