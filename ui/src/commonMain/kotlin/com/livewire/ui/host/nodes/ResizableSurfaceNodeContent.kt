package com.livewire.ui.host.nodes

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.livewire.ui.host.LayoutNodeContent
import com.livewire.ui.host.debugFrame
import com.livewire.ui.util.thenIf
import com.livewire.ui.util.thenIfElse
import com.livewire.ui.widget.ResizableSurfaceNode
import com.livewire.ui.widget.ResizeAnchor

@Composable
internal fun ResizableSurfaceNodeContent(
  node: ResizableSurfaceNode,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current

  var currentSize by remember { mutableStateOf(node.initialSize) }

  val shape = node.shape.toComposeUi()
  val color = node.color ?: MaterialTheme.colorScheme.surface
  val contentColor = node.contentColor ?: contentColorFor(color)

  Box(
    modifier = modifier
      .debugFrame()
      .thenIfElse(
        node.anchor.isHorizontal,
        ifTrue = {
          width(currentSize)
        },
        ifFalse = {
          height(currentSize)
        }
      ),
  ) {
    Surface(
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = node.tonalElevation,
      shadowElevation = node.shadowElevation,
      modifier = Modifier
        .fillMaxSize()
        .zIndex(0f),
    ) {
      node.children.forEach { child ->
        LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
      }
    }

    // Drag handle at bottom-end corner
    ResizeHandle(
      anchor = node.anchor,
      onDrag = { dragAmount ->
        with(density) {
          val newSize = currentSize - if (node.anchor.isHorizontal) {
            dragAmount.x.toDp()
          } else {
            dragAmount.y.toDp()
          }
          currentSize = newSize.coerceIn(node.minSize, node.maxSize)
        }
      },
      modifier = Modifier
        .zIndex(1f),
    )
  }
}

@Composable
private fun BoxScope.ResizeHandle(
  anchor: ResizeAnchor,
  onDrag: (Offset) -> Unit,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  var isDragging by remember { mutableStateOf(false) }

  val baseModifier = if (anchor.isHorizontal) {
    modifier
      .thenIf(anchor == ResizeAnchor.Start) {
        align(Alignment.CenterStart)
      }
      .thenIf(anchor == ResizeAnchor.End) {
        align(Alignment.CenterEnd)
      }
      .width(DragHandleSize)
      .fillMaxHeight()
  } else {
    modifier
      .thenIf(anchor == ResizeAnchor.Top) {
        align(Alignment.TopCenter)
      }
      .thenIf(anchor == ResizeAnchor.Bottom) {
        align(Alignment.BottomCenter)
      }
      .height(DragHandleSize)
      .fillMaxWidth()
  }

  Box(
    modifier = baseModifier
      .cursorForHorizontalResize(anchor.isHorizontal)
      .hoverable(interactionSource)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = {
            isDragging = true
          },
          onDragEnd = {
            isDragging = false
          }
        ) { change, dragAmount ->
          change.consume()
          onDrag(dragAmount)
        }
      },
  )
}

private val DragHandleSize = 8.dp

expect fun Modifier.cursorForHorizontalResize(isHorizontal: Boolean): Modifier
