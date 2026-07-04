package com.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.livewire.ui.host.debugFrame
import com.livewire.ui.host.nodes.composables.TableLayout
import com.livewire.ui.host.nodes.icons.FirstPage
import com.livewire.ui.host.nodes.icons.HostIcons
import com.livewire.ui.host.nodes.icons.KeyboardArrowLeft
import com.livewire.ui.host.nodes.icons.KeyboardArrowRight
import com.livewire.ui.host.nodes.icons.LastPage
import com.livewire.ui.widget.TableNode

@Composable
internal fun TableNodeContent(
  node: TableNode,
  modifier: Modifier = Modifier,
) {
  var currentPage by remember { mutableIntStateOf(0) }

  val totalPages = if (node.rows.isEmpty()) {
    1
  } else {
    (node.rows.size + node.pageSize - 1) / node.pageSize
  }

  // Clamp currentPage if rows shrink
  if (currentPage >= totalPages) {
    currentPage = (totalPages - 1).coerceAtLeast(0)
  }

  val startIndex = currentPage * node.pageSize
  val endIndex = (startIndex + node.pageSize).coerceAtMost(node.rows.size)
  val pageRows = if (node.rows.isNotEmpty()) node.rows.subList(startIndex, endIndex) else emptyList()


  Column(modifier.debugFrame()) {
    TableLayout(
      columns = node.columns,
      rows = pageRows,
      modifier = modifier.weight(1f),
    )


    // Bottom pagination bar
    HorizontalDivider()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
    ) {
      IconButton(
        onClick = { currentPage = 0 },
        enabled = currentPage > 0,
      ) {
        Icon(HostIcons.FirstPage, contentDescription = null)
      }
      IconButton(
        onClick = { currentPage-- },
        enabled = currentPage > 0,
      ) {
        Icon(HostIcons.KeyboardArrowLeft, contentDescription = null)
      }
      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("${currentPage + 1}")
          }
          append(" of ")
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append("${totalPages}")
          }
        },
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
      IconButton(
        onClick = { currentPage++ },
        enabled = currentPage < totalPages - 1,
      ) {
        Icon(HostIcons.KeyboardArrowRight, contentDescription = null)
      }
      IconButton(
        onClick = { currentPage = totalPages - 1 },
        enabled = currentPage < totalPages - 1,
      ) {
        Icon(HostIcons.LastPage, contentDescription = null)
      }
    }
  }
}
