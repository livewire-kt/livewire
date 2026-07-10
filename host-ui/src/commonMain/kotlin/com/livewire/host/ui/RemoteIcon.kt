package com.livewire.host.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.widget.IconNode

/**
 * Renders an [IconNode] received over the wire.
 */
@Composable
fun RemoteIcon(
  node: IconNode,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = node.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
) {
  RemoteIcon(
    vector = node.vector,
    contentDescription = contentDescription,
    tint = tint,
    modifier = modifier,
  )
}

/**
 * Renders a [VectorIcon] received over the wire; renders nothing when [vector] is null.
 */
@Composable
fun RemoteIcon(
  vector: VectorIcon?,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  if (vector != null) {
    Icon(
      imageVector = remember(vector) { vector.toImageVector() },
      contentDescription = contentDescription,
      tint = tint,
      modifier = modifier,
    )
  }
}
