package com.r0adkll.livewire.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PluginDrawerItem(
  selected: Boolean,
  info: PluginInfo,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalPlatformContext.current
  val imageRequest = remember(info.iconData) {
    ImageRequest.Builder(context)
      .data(ByteBuffer.wrap(info.iconData.toByteArray()))
      .decoderFactory(SvgDecoder.Factory())
      .build()
  }

  val containerColor by animateColorAsState(
    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
  )
  val contentColor by animateColorAsState(
    if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
  )

  Surface(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    color = containerColor,
    contentColor = contentColor,
    modifier = modifier
      .fillMaxWidth(),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .background(
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small,
          ),
        contentAlignment = Alignment.Center,
      ) {
        AsyncImage(
          model = imageRequest,
          contentDescription = null,
          colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
          modifier = Modifier.size(18.dp),
        )
      }

      Spacer(Modifier.width(16.dp))

      Text(
        text = info.title,
        style = MaterialTheme.typography.titleMediumEmphasized,
      )
    }
  }
}
