package com.r0adkll.livewire.plugin.host.composables.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Chat: ImageVector
  get() {
    if (_Chat != null) {
      return _Chat!!
    }
    _Chat = ImageVector.Builder(
      name = "Chat",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 960f,
      viewportHeight = 960f
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
        moveTo(280f, 560f)
        horizontalLineToRelative(240f)
        quadToRelative(17f, 0f, 28.5f, -11.5f)
        reflectiveQuadTo(560f, 520f)
        quadToRelative(0f, -17f, -11.5f, -28.5f)
        reflectiveQuadTo(520f, 480f)
        lineTo(280f, 480f)
        quadToRelative(-17f, 0f, -28.5f, 11.5f)
        reflectiveQuadTo(240f, 520f)
        quadToRelative(0f, 17f, 11.5f, 28.5f)
        reflectiveQuadTo(280f, 560f)
        close()
        moveTo(280f, 440f)
        horizontalLineToRelative(400f)
        quadToRelative(17f, 0f, 28.5f, -11.5f)
        reflectiveQuadTo(720f, 400f)
        quadToRelative(0f, -17f, -11.5f, -28.5f)
        reflectiveQuadTo(680f, 360f)
        lineTo(280f, 360f)
        quadToRelative(-17f, 0f, -28.5f, 11.5f)
        reflectiveQuadTo(240f, 400f)
        quadToRelative(0f, 17f, 11.5f, 28.5f)
        reflectiveQuadTo(280f, 440f)
        close()
        moveTo(280f, 320f)
        horizontalLineToRelative(400f)
        quadToRelative(17f, 0f, 28.5f, -11.5f)
        reflectiveQuadTo(720f, 280f)
        quadToRelative(0f, -17f, -11.5f, -28.5f)
        reflectiveQuadTo(680f, 240f)
        lineTo(280f, 240f)
        quadToRelative(-17f, 0f, -28.5f, 11.5f)
        reflectiveQuadTo(240f, 280f)
        quadToRelative(0f, 17f, 11.5f, 28.5f)
        reflectiveQuadTo(280f, 320f)
        close()
      }
    }.build()

    return _Chat!!
  }

@Suppress("ObjectPropertyName")
private var _Chat: ImageVector? = null
