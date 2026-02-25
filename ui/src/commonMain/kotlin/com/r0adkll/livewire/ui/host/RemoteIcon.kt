package com.r0adkll.livewire.ui.host

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import java.nio.ByteBuffer

@Composable
fun RemoteIcon(
  svgData: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current
) {
  val context = LocalPlatformContext.current
  val imageRequest = remember(svgData) {
    ImageRequest.Builder(context)
      .data(ByteBuffer.wrap(svgData.toByteArray()))
      .decoderFactory(SvgDecoder.Factory())
      .build()
  }

  val painter = rememberAsyncImagePainter(imageRequest)
  Icon(
    painter = painter,
    contentDescription = contentDescription,
    modifier = modifier,
    tint = tint,
  )
}
