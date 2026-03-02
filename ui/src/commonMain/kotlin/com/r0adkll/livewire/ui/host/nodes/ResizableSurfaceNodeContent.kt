package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.ResizableSurfaceNode

@Composable
internal fun ResizableSurfaceNodeContent(
  node: ResizableSurfaceNode,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current

  val minWidth = node.minWidth.dp
  val minHeight = node.minHeight.dp
  val maxWidth = if (node.maxWidth == Float.MAX_VALUE) Dp.Infinity else node.maxWidth.dp
  val maxHeight = if (node.maxHeight == Float.MAX_VALUE) Dp.Infinity else node.maxHeight.dp

  var currentWidth by remember { mutableStateOf(node.initialWidth.dp) }
  var currentHeight by remember { mutableStateOf(node.initialHeight.dp) }

  val shape = node.shape.toComposeUi()
  val color = node.color ?: MaterialTheme.colorScheme.surface
  val contentColor = node.contentColor ?: contentColorFor(color)
  val tonalElevation = node.tonalElevation.dp
  val shadowElevation = node.shadowElevation.dp

  Box(
    modifier = modifier
      .debugFrame()
      .size(currentWidth),
  ) {
    Surface(
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = tonalElevation,
      shadowElevation = shadowElevation,
      modifier = Modifier.fillMaxSize(),
    ) {
      Box {
        node.children.forEach { child ->
          LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
        }
      }
    }

    // Drag handle at bottom-end corner
    ResizeHandle(
      onDrag = { dragAmount ->
        with(density) {
          val newWidth = currentWidth - dragAmount.x.toDp()
          currentWidth = newWidth.coerceIn(minWidth, maxWidth)
        }
      },
      modifier = Modifier.align(Alignment.BottomStart),
    )
  }
}

@Composable
private fun ResizeHandle(
  onDrag: (Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  val handleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

  Box(
    modifier = modifier
      .size(24.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          onDrag(dragAmount)
        }
      }
      .drawBehind {
        val strokeWidth = 1.5.dp.toPx()
        val padding = 4.dp.toPx()
        val spacing = 5.dp.toPx()

        // Draw three diagonal lines (classic resize grip pattern)
        for (i in 0..2) {
          val offset = i * spacing
          drawLine(
            color = handleColor,
            end = Offset(size.width - padding - offset, size.height - padding),
            start = Offset(size.width - padding, size.height - padding - offset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
          )
        }
      },
  )
}
