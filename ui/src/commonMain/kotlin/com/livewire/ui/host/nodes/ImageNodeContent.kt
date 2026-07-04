package com.livewire.ui.host.nodes

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.ImageNode

@Composable
internal fun ImageNodeContent(
  node: ImageNode,
  modifier: Modifier = Modifier,
) {
  val context = LocalPlatformContext.current
  val imageRequest = remember(node.imageData) {
    ImageRequest.Builder(context)
      .data(node.imageData)
      .build()
  }
  val painter = rememberAsyncImagePainter(imageRequest)
  Image(
    painter = painter,
    contentDescription = node.contentDescription,
    contentScale = ContentScale.Fit,
    modifier = modifier.debugFrame(),
  )
}
