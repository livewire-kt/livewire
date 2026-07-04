package com.livewire.layout

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.ui.layout.ColumnNode
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RootNode
import com.livewire.ui.widget.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SnapshotChildrenTest {
  @Test
  fun testDecodePreservesStructureForProtobuf() {
    val strategy = LayoutNodeSerialization.Protobuf.toStrategy()
    val decoded = strategy.decodeFromByteArray(strategy.encodeToByteArray(sampleTree()))
    val column = decoded.children.single() as ColumnNode
    assertEquals(listOf("a", "b"), column.children.map { (it as TextNode).text })
  }

  @Test
  fun testMakeObservableConvertsEveryNode() {
    val strategy = LayoutNodeSerialization.Protobuf.toStrategy()
    val decoded = strategy.decodeFromByteArray(strategy.encodeToByteArray(sampleTree()))
    assertTrue(decoded.children.single().children.first().children !is SnapshotStateList<*>)
    decoded.makeObservable()
    assertAllObservable(decoded)
  }

  @Test
  fun clientBuiltNodesKeepPlainLists() {
    assertTrue(TextNode(text = "x").children !is SnapshotStateList<*>)
    assertTrue(RootNode().children !is SnapshotStateList<*>)
  }
}

private fun sampleTree(): RootNode = RootNode().apply {
  children.add(
    ColumnNode().apply {
      children.add(TextNode(text = "a"))
      children.add(TextNode(text = "b"))
    },
  )
}

private fun assertAllObservable(node: LayoutNode) {
  assertTrue(node.children is SnapshotStateList<*>, "${node::class.simpleName} children must be observable")
  node.children.forEach { assertAllObservable(it) }
}
