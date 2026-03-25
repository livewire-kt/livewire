package com.r0adkll.livewire.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.util.asReadableBytes

@Composable
internal fun OverviewScreen(
  currentSize: Long,
  sizeHistory: SnapshotStateList<Long>,
  messages: List<String>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    NodeSizeGraph(
      dataPoints = sizeHistory,
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f),
    )

    Text(
      text = "Messages",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    )

    MessagePage(
      messages = messages,
      modifier = Modifier
        .weight(5f)
    )
  }
}

@Composable
private fun NodeSizeGraph(
  dataPoints: List<Long>,
  modifier: Modifier = Modifier,
) {
  val lineColor = MaterialTheme.colorScheme.primary
  val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val labelStyle = MaterialTheme.typography.labelSmall

  if (dataPoints.size < 2) {
    Canvas(modifier = modifier) {
      // Draw empty state - just the baseline
      drawLine(
        color = gridColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1f,
      )
    }
    return
  }

  // Compute axis labels outside Canvas since we need text measurer
  val maxValue = dataPoints.max().coerceAtLeast(1L)
  val minValue = dataPoints.min()

  Column(modifier = modifier) {
    // Y-axis labels
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "min: ${minValue.asReadableBytes()}",
        style = labelStyle,
        color = labelColor,
      )
      Text(
        text = "max: ${maxValue.asReadableBytes()}",
        style = labelStyle,
        color = labelColor,
      )
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(top = 4.dp),
    ) {
      val canvasWidth = size.width
      val canvasHeight = size.height
      val topPadding = 8f
      val graphHeight = canvasHeight - topPadding

      // Draw horizontal grid lines
      val gridLines = 4
      for (i in 0..gridLines) {
        val y = topPadding + (graphHeight * i / gridLines)
        drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(canvasWidth, y),
          strokeWidth = 1f,
        )
      }

      // Map data points to canvas coordinates
      val range = (maxValue - minValue).coerceAtLeast(1L).toFloat()
      val stepX = canvasWidth / (dataPoints.size - 1).toFloat()

      val linePath = Path()
      val fillPath = Path()

      dataPoints.forEachIndexed { index, value ->
        val x = index * stepX
        val normalized = (value - minValue).toFloat() / range
        val y = topPadding + graphHeight * (1f - normalized)

        if (index == 0) {
          linePath.moveTo(x, y)
          fillPath.moveTo(x, canvasHeight)
          fillPath.lineTo(x, y)
        } else {
          linePath.lineTo(x, y)
          fillPath.lineTo(x, y)
        }
      }

      // Close fill path
      fillPath.lineTo(canvasWidth, canvasHeight)
      fillPath.close()

      // Draw filled area
      drawPath(
        path = fillPath,
        color = fillColor,
      )

      // Draw line
      drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 2f),
      )
    }

    // Data point count
    Text(
      text = "${dataPoints.size} samples",
      style = labelStyle,
      color = labelColor,
    )
  }
}

@Composable
private fun MessagePage(
  messages: List<String>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    reverseLayout = true,
  ) {
    items(messages) { msg ->
      Text(msg, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
