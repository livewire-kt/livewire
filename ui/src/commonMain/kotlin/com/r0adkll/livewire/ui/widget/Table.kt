package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Table(
  columns: List<String>,
  rows: List<List<String>>,
  modifier: LivewireModifier = LivewireModifier,
  pageSize: Int = 10,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<TableNode, Applier<LayoutNode>>(
    factory = { TableNode(columns, rows, pageSize) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(columns, TableNode.SetColumns)
      set(rows, TableNode.SetRows)
      set(pageSize, TableNode.SetPageSize)
    },
  )
}

@LivewireSerializer
@Serializable
class TableNode(
  var columns: List<String>,
  var rows: List<List<String>>,
  var pageSize: Int = 10,
) : LayoutNode() {

  companion object {
    val SetColumns: TableNode.(List<String>) -> Unit = { columns = it }
    val SetRows: TableNode.(List<List<String>>) -> Unit = { rows = it }
    val SetPageSize: TableNode.(Int) -> Unit = { pageSize = it }
  }
}
