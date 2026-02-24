package com.r0adkll.livewire.ui.layout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.TextNode
import com.r0adkll.livewire.ui.widget.TextStyle
import kotlinx.coroutines.launch
import com.r0adkll.livewire.ui.layout.Alignment as LivewireAlignment

@Composable
internal fun LayoutNodeContent(
  node: LayoutNode,
  modifier: Modifier,
) {
  when (node) {
    is BoxNode -> BoxNodeContent(node, modifier)
    is ColumnNode -> ColumnNodeContent(node, modifier)
    is RowNode -> RowNodeContent(node, modifier)
    is TextNode -> TextNodeContent(node, modifier)
    is ButtonNode -> ButtonNodeContent(node, modifier)

    else -> {
      Box(modifier.debugFrame()) {
        node.children.forEach { child ->
          LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
        }
      }
    }
  }
}

@Composable
private fun BoxNodeContent(
  node: BoxNode,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.debugFrame(),
    contentAlignment = when (node.contentAlignment) {
      LivewireAlignment.BottomCenter -> Alignment.BottomCenter
      LivewireAlignment.BottomEnd -> Alignment.BottomEnd
      LivewireAlignment.BottomStart -> Alignment.BottomStart
      LivewireAlignment.Center -> Alignment.Center
      LivewireAlignment.CenterEnd -> Alignment.CenterEnd
      LivewireAlignment.CenterStart -> Alignment.CenterStart
      else -> Alignment.TopStart
    }
  ) {
    node.children.forEach { child ->
      val modifier = with (child.modifier) { this@Box.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}

@Composable
private fun ColumnNodeContent(
  node: ColumnNode,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.debugFrame(),
    horizontalAlignment = when (node.horizontalAlignment) {
      LivewireAlignment.CenterHorizontally -> Alignment.CenterHorizontally
      LivewireAlignment.End -> Alignment.End
      LivewireAlignment.Start -> Alignment.Start
    }
  ) {
    node.children.forEach { child ->
      val modifier = with (child.modifier) { this@Column.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}

@Composable
private fun RowNodeContent(
  node: RowNode,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.debugFrame(),
    verticalAlignment = when (node.verticalAlignment) {
      LivewireAlignment.Bottom -> Alignment.Bottom
      LivewireAlignment.CenterVertically -> Alignment.CenterVertically
      LivewireAlignment.Top -> Alignment.Top
    }
  ) {
    node.children.forEach { child ->
      val modifier = with (child.modifier) { this@Row.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}

@Composable
private fun TextNodeContent(
  node: TextNode,
  modifier: Modifier = Modifier,
) {
  Text(
    text = node.text,
    style = when (node.style) {
      TextStyle.DisplayLarge -> MaterialTheme.typography.displayLarge
      TextStyle.DisplayMedium -> MaterialTheme.typography.displayMedium
      TextStyle.DisplaySmall -> MaterialTheme.typography.displaySmall
      TextStyle.HeadlineLarge -> MaterialTheme.typography.headlineLarge
      TextStyle.HeadlineMedium -> MaterialTheme.typography.headlineMedium
      TextStyle.HeadlineSmall -> MaterialTheme.typography.headlineSmall
      TextStyle.TitleLarge -> MaterialTheme.typography.titleLarge
      TextStyle.TitleMedium -> MaterialTheme.typography.titleMedium
      TextStyle.TitleSmall -> MaterialTheme.typography.titleSmall
      TextStyle.BodyLarge -> MaterialTheme.typography.bodyLarge
      TextStyle.BodyMedium -> MaterialTheme.typography.bodyMedium
      TextStyle.BodySmall -> MaterialTheme.typography.bodySmall
      TextStyle.LabelLarge -> MaterialTheme.typography.labelLarge
      TextStyle.LabelMedium -> MaterialTheme.typography.labelMedium
      TextStyle.LabelSmall -> MaterialTheme.typography.labelSmall
      null -> LocalTextStyle.current
    },
    fontWeight = node.fontWeight?.let { FontWeight(it) },
    modifier = modifier.debugFrame(),
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ButtonNodeContent(
  node: ButtonNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val buttonSize = when (node.size) {
    ButtonSize.ExtraSmall -> ButtonDefaults.ExtraSmallContainerHeight
    ButtonSize.Small -> ButtonDefaults.MinHeight
    ButtonSize.Medium -> ButtonDefaults.MediumContainerHeight
    ButtonSize.Large -> ButtonDefaults.LargeContainerHeight
  }

  Button(
    onClick = {
      scope.launch {
        eventDispatcher.dispatch(node.action)
      }
    },
    shapes = ButtonDefaults.shapes(),
    modifier = modifier
      .heightIn(buttonSize)
      .debugFrame(),
    contentPadding = ButtonDefaults.contentPaddingFor(buttonSize),
  ) {
    Text(
      text = node.text,
      style = ButtonDefaults.textStyleFor(buttonSize),
    )
  }
}

/**
 * Set this to true to draw debugging information on the screen
 */
const val DebugNodes = false

fun Modifier.debugFrame(): Modifier = if (DebugNodes) {
  border(
    width = 1.dp,
    color = Color.Red
  )
} else this
