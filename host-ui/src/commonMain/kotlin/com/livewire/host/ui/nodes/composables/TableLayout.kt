package com.livewire.host.ui.nodes.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TableLayout(
  columns: List<String>,
  rows: List<List<String>>,
  modifier: Modifier = Modifier,
  stickyRowCount: Int = 1,
  stickyColumnCount: Int = 0,
  columnName: @Composable ((name: String, rowIndex: Int, columnIndex: Int) -> Unit) = { name, _, _ ->
    TableLayoutDefaults.ColumnName(
      name = name,
    )
  },
  content: @Composable (value: String, rowIndex: Int, columnIndex: Int) -> Unit = { value, row, column ->
    TableLayoutDefaults.ColumnValue(
      value = value,
      rowIndex = row,
      columnIndex = column,
    )
  }
) {
  TableLayout(
    rowCount = rows.size + 1,
    columnCount = columns.size,
    stickyRowCount = stickyRowCount,
    stickyColumnCount = stickyColumnCount,
    modifier = modifier,
  ) { rowIndex, columnIndex ->
    if (rowIndex == 0) {
      val value = columns[columnIndex]
      columnName(value, rowIndex, columnIndex)
    } else {
      val value = rows[rowIndex - 1][columnIndex]
      content(value, rowIndex - 1, columnIndex)
    }
  }
}

@Composable
fun TableLayout(
  modifier: Modifier = Modifier,
  rowCount: Int,
  columnCount: Int,
  stickyRowCount: Int = 0,
  stickyColumnCount: Int = 0,
  maxCellWidthDp: Dp = Dp.Infinity,
  maxCellHeightDp: Dp = Dp.Infinity,
  verticalScrollState: ScrollState = rememberScrollState(),
  horizontalScrollState: ScrollState = rememberScrollState(),
  cellContent: @Composable (rowIndex: Int, columnIndex: Int) -> Unit
) {
  val columnWidths = remember { mutableStateMapOf<Int, Int>() }
  val rowHeights = remember { mutableStateMapOf<Int, Int>() }

  if (columnCount != columnWidths.size || rowCount != rowHeights.size) {
    columnWidths.clear()
    rowHeights.clear()
  }

  val maxCellWidth = if (listOf(Dp.Infinity, Dp.Unspecified).contains(maxCellWidthDp)) {
    Constraints.Infinity
  } else {
    with(LocalDensity.current) { maxCellWidthDp.toPx() }.toInt()
  }
  val maxCellHeight = if (listOf(Dp.Infinity, Dp.Unspecified).contains(maxCellHeightDp)) {
    Constraints.Infinity
  } else {
    with(LocalDensity.current) { maxCellHeightDp.toPx() }.toInt()
  }

  // not using mutableStateListOf because the list is entirely replaced on mutations
  var accumWidths by remember { mutableStateOf(listOf<Int>()) }
  var accumHeights by remember { mutableStateOf(listOf<Int>()) }

  @Composable
  fun StickyCells(modifier: Modifier = Modifier, rowCount: Int, columnCount: Int) {
    if (rowCount > 0 && columnCount > 0) {
      Box(modifier = modifier) {
        Layout(
          content = {
            (0 until rowCount).forEach { rowIndex ->
              (0 until columnCount).forEach { columnIndex ->
                cellContent(rowIndex, columnIndex)
              }
            }
          },
        ) { measurables, constraints ->
          val placeables = measurables.mapIndexed { index, it ->
            val columnIndex = index % columnCount
            val rowIndex = index / columnCount
            it.measure(
              Constraints(
                minWidth = columnWidths[columnIndex] ?: 0,
                minHeight = rowHeights[rowIndex] ?: 0,
                maxWidth = columnWidths[columnIndex] ?: 0,
                maxHeight = rowHeights[rowIndex] ?: 0
              )
            )
          }

          val totalWidth = accumWidths[columnCount]
          val totalHeight = accumHeights[rowCount]

          layout(width = totalWidth, height = totalHeight) {
            placeables.forEachIndexed { index, placeable ->
              val columnIndex = index % columnCount
              val rowIndex = index / columnCount

              placeable.placeRelative(
                accumWidths[columnIndex],
                accumHeights[rowIndex]
              )
            }
          }
        }
      }
    }
  }

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .then(Modifier.horizontalScroll(horizontalScrollState))
        .then(Modifier.verticalScroll(verticalScrollState))
    ) {
      Layout(
        content = {
          (0 until rowCount).forEach { rowIndex ->
            (0 until columnCount).forEach { columnIndex ->
              cellContent(rowIndex, columnIndex)
            }
          }
        },
      ) { measurables, constraints ->
        val placeables = measurables.mapIndexed { index, it ->
          val columnIndex = index % columnCount
          val rowIndex = index / columnCount
          it.measure(
            Constraints(
              minWidth = columnWidths[columnIndex] ?: 0,
              minHeight = rowHeights[rowIndex] ?: 0,
              maxWidth = maxCellWidth,
              maxHeight = maxCellHeight
            )
          )
        }

        placeables.forEachIndexed { index, placeable ->
          val columnIndex = index % columnCount
          val rowIndex = index / columnCount

          val existingWidth = columnWidths[columnIndex] ?: 0
          val maxWidth = maxOf(existingWidth, placeable.width)
          if (maxWidth > existingWidth || (existingWidth == 0 && maxWidth == existingWidth)) {
            columnWidths[columnIndex] = maxWidth
          }

          val existingHeight = rowHeights[rowIndex] ?: 0
          val maxHeight = maxOf(existingHeight, placeable.height)
          if (maxHeight > existingHeight || (existingHeight == 0 && maxHeight == existingHeight)) {
            rowHeights[rowIndex] = maxHeight
          }
        }

        accumWidths = mutableListOf(0).apply {
          (1..columnWidths.size).forEach { i ->
            this += this.last() + columnWidths[i - 1]!!
          }
        }
        accumHeights = mutableListOf(0).apply {
          (1..rowHeights.size).forEach { i ->
            this += this.last() + rowHeights[i - 1]!!
          }
        }

        val totalWidth = accumWidths.last()
        val totalHeight = accumHeights.last()

        layout(width = totalWidth, height = totalHeight) {
          placeables.forEachIndexed { index, placeable ->
            val columnIndex = index % columnCount
            val rowIndex = index / columnCount

            placeable.placeRelative(accumWidths[columnIndex], accumHeights[rowIndex])
          }
        }
      }
    }

    if (columnWidths.isEmpty() || rowHeights.isEmpty()) {
      return@Box
    }

    StickyCells(
      modifier = Modifier.horizontalScroll(horizontalScrollState),
      rowCount = stickyRowCount,
      columnCount = columnCount
    )

    StickyCells(
      modifier = Modifier.verticalScroll(verticalScrollState),
      rowCount = rowCount,
      columnCount = stickyColumnCount
    )

    StickyCells(
      rowCount = stickyRowCount,
      columnCount = stickyColumnCount
    )
  }
}

object TableLayoutDefaults {

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  @Composable
  fun ColumnName(
    name: String,
    modifier: Modifier = Modifier,
  ) {
    Box(
      modifier = modifier
        .background(
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        .padding(
          horizontal = 16.dp,
          vertical = 8.dp
        ),
      contentAlignment = Alignment.CenterStart,
    ) {
      Text(
        text = name,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  @Composable
  fun ColumnValue(
    value: String,
    rowIndex: Int,
    columnIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    offContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
  ) {
    val color = if (rowIndex % 2 == 0) containerColor else offContainerColor
    val contentColor = contentColorFor(color)
    Box(
      modifier = modifier
        .background(color)
        .padding(
          horizontal = 16.dp,
          vertical = 8.dp
        ),
      contentAlignment = Alignment.CenterStart,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.labelMediumEmphasized,
        color = contentColor,
      )
    }
  }
}
