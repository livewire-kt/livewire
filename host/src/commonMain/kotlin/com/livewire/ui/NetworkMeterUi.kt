package com.livewire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.livewire.runtime.LivewireHost
import com.livewire.runtime.LivewireHostConnection
import com.livewire.theme.LivewireThemeContent
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.util.asReadableBytes
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val MaxDataPoints = 60
private const val MaxMessages = 100

@Composable
internal fun NetworkMeterWindow(
  host: LivewireHost,
  theme: LivewireTheme,
  darkMode: Boolean,
  onCloseRequest: () -> Unit,
) {
  Window(
    onCloseRequest = onCloseRequest,
    title = "Livewire Network Meter",
    state = rememberWindowState(size = DpSize(480.dp, 720.dp)),
  ) {
    LivewireThemeContent(
      theme = theme,
      darkMode = darkMode,
      host = host,
    ) {
      Surface(modifier = Modifier.fillMaxSize()) {
        NetworkMeterUi(connection = host.connection)
      }
    }
  }
}

@Composable
internal fun NetworkMeterUi(
  connection: LivewireHostConnection,
  modifier: Modifier = Modifier,
) {
  val connectionState by connection.connectionState.collectAsState()
  val lastFrameSize by connection.incomingLayoutSize.collectAsState()
  val totalBytes by connection.incomingLayoutBytesTotal.collectAsState()

  val frameSizeHistory = remember { mutableStateListOf<Long>() }
  LaunchedEffect(connection) {
    var lastTotal = connection.incomingLayoutBytesTotal.value
    connection.incomingLayoutBytesTotal.collect { total ->
      val delta = total - lastTotal
      lastTotal = total
      if (delta > 0L) {
        frameSizeHistory.add(delta)
        if (frameSizeHistory.size > MaxDataPoints) frameSizeHistory.removeAt(0)
      }
    }
  }

  var throughput by remember { mutableStateOf(0L) }
  val throughputHistory = remember { mutableStateListOf<Long>() }
  LaunchedEffect(connection) {
    var lastTotal = connection.incomingLayoutBytesTotal.value
    while (true) {
      delay(1.seconds)
      val total = connection.incomingLayoutBytesTotal.value
      throughput = (total - lastTotal).coerceAtLeast(0L)
      lastTotal = total
      throughputHistory.add(throughput)
      if (throughputHistory.size > MaxDataPoints) throughputHistory.removeAt(0)
    }
  }

  val messages = remember { mutableStateListOf<String>() }
  LaunchedEffect(connection) {
    connection.incomingMessages.collect { message ->
      messages.add(message.toString())
      if (messages.size > MaxMessages) messages.removeAt(0)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MeterStat(
        label = "State",
        value = connectionState.name,
        modifier = Modifier.weight(1f),
      )
      MeterStat(
        label = "Throughput",
        value = "${throughput.asReadableBytes()}/s",
        modifier = Modifier.weight(1f),
      )
      MeterStat(
        label = "Last Frame",
        value = lastFrameSize.asReadableBytes(),
        modifier = Modifier.weight(1f),
      )
      MeterStat(
        label = "Total",
        value = totalBytes.asReadableBytes(),
        modifier = Modifier.weight(1f),
      )
    }

    MeterSectionTitle("Layout Throughput")
    MeterGraph(
      dataPoints = throughputHistory,
      formatValue = { "${it.asReadableBytes()}/s" },
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f),
    )

    MeterSectionTitle("Frame Sizes")
    MeterGraph(
      dataPoints = frameSizeHistory,
      formatValue = { it.asReadableBytes() },
      modifier = Modifier
        .fillMaxWidth()
        .weight(2f),
    )

    MeterSectionTitle("Messages")
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(3f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
      reverseLayout = true,
    ) {
      items(messages) { msg ->
        Text(msg, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

@Composable
private fun MeterStat(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    tonalElevation = 1.dp,
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
      )
    }
  }
}

@Composable
private fun MeterSectionTitle(
  title: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 8.dp),
  )
}

@Composable
private fun MeterGraph(
  dataPoints: List<Long>,
  formatValue: (Long) -> String,
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

  val maxValue = dataPoints.max().coerceAtLeast(1L)
  val minValue = dataPoints.min()

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "min: ${formatValue(minValue)}",
        style = labelStyle,
        color = labelColor,
      )
      Text(
        text = "max: ${formatValue(maxValue)}",
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

      drawPath(
        path = fillPath,
        color = fillColor,
      )

      drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 2f),
      )
    }

    Text(
      text = "${dataPoints.size} samples",
      style = labelStyle,
      color = labelColor,
    )
  }
}
