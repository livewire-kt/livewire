package com.livewire.data

import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.widget.TableNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
class TableNodeProtobufTest {

  @Test
  fun tableNodeRoundTripsThroughProtobuf() {
    val node = TableNode(
      columns = listOf("Name", "Role"),
      rows = listOf(
        listOf("Alice", "Engineer"),
        listOf("Bob", "Designer"),
      ),
      pageSize = 10,
    )

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<TableNode>(bytes)

    assertEquals(node.columns, decoded.columns)
    assertEquals(node.rows, decoded.rows)
    assertEquals(node.pageSize, decoded.pageSize)
  }
}
