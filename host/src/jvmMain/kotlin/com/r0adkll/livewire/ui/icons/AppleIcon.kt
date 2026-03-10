package com.r0adkll.livewire.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppleIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
  ImageVector.Builder(
    name = "apple",
    defaultWidth = 50.dp,
    defaultHeight = 50.dp,
    viewportWidth = 50f,
    viewportHeight = 50f
  ).apply {
    path(fill = SolidColor(Color(0xFF000000))) {
      moveTo(44.527344f, 34.75f)
      curveTo(43.449219f, 37.144531f, 42.929688f, 38.214844f, 41.542969f, 40.328125f)
      curveTo(39.601563f, 43.28125f, 36.863281f, 46.96875f, 33.480469f, 46.992188f)
      curveTo(30.46875f, 47.019531f, 29.691406f, 45.027344f, 25.601563f, 45.0625f)
      curveTo(21.515625f, 45.082031f, 20.664063f, 47.03125f, 17.648438f, 47f)
      curveTo(14.261719f, 46.96875f, 11.671875f, 43.648438f, 9.730469f, 40.699219f)
      curveTo(4.300781f, 32.429688f, 3.726563f, 22.734375f, 7.082031f, 17.578125f)
      curveTo(9.457031f, 13.921875f, 13.210938f, 11.773438f, 16.738281f, 11.773438f)
      curveTo(20.332031f, 11.773438f, 22.589844f, 13.746094f, 25.558594f, 13.746094f)
      curveTo(28.441406f, 13.746094f, 30.195313f, 11.769531f, 34.351563f, 11.769531f)
      curveTo(37.492188f, 11.769531f, 40.8125f, 13.480469f, 43.1875f, 16.433594f)
      curveTo(35.421875f, 20.691406f, 36.683594f, 31.78125f, 44.527344f, 34.75f)
      close()
      moveTo(31.195313f, 8.46875f)
      curveTo(32.707031f, 6.527344f, 33.855469f, 3.789063f, 33.4375f, 1f)
      curveTo(30.972656f, 1.167969f, 28.089844f, 2.742188f, 26.40625f, 4.78125f)
      curveTo(24.878906f, 6.640625f, 23.613281f, 9.398438f, 24.105469f, 12.066406f)
      curveTo(26.796875f, 12.152344f, 29.582031f, 10.546875f, 31.195313f, 8.46875f)
      close()
    }
  }.build()
}
