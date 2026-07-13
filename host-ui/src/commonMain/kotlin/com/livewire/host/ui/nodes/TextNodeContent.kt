package com.livewire.host.ui.nodes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.TextNode

@Composable
internal fun TextNodeContent(
  node: TextNode,
  modifier: Modifier = Modifier,
) {
  Text(
    text = node.text,
    color = node.color,
    style = node.style.asComposeTextStyle,
    fontWeight = node.fontWeight?.let { FontWeight(it) },
    modifier = modifier.debugFrame(),
  )
}
