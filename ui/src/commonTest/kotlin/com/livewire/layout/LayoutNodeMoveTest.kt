package com.livewire.layout

import com.livewire.ui.layout.RootNode
import com.livewire.ui.widget.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutNodeMoveTest {
  @Test
  fun testSingleForward() {
    val nodes = nodes("A", "B", "C", "D", "E")
    nodes.move(from = 1, to = 3, count = 1)
    assertEquals(listOf("A", "C", "B", "D", "E"), nodes.labels())
  }

  @Test
  fun testSingleBackward() {
    val nodes = nodes("A", "B", "C", "D", "E")
    nodes.move(from = 3, to = 1, count = 1)
    assertEquals(listOf("A", "D", "B", "C", "E"), nodes.labels())
  }

  @Test
  fun testMultiForward() {
    val nodes = nodes("A", "B", "C", "D", "E")
    nodes.move(from = 0, to = 3, count = 2)
    assertEquals(listOf("C", "A", "B", "D", "E"), nodes.labels())
  }

  @Test
  fun testMultiBackward() {
    val nodes = nodes("A", "B", "C", "D", "E")
    nodes.move(from = 3, to = 0, count = 2)
    assertEquals(listOf("D", "E", "A", "B", "C"), nodes.labels())
  }

  @Test
  fun testMultiBackwardMiddle() {
    val nodes = nodes("A", "B", "C", "D", "E")
    nodes.move(from = 3, to = 1, count = 2)
    assertEquals(listOf("A", "D", "E", "B", "C"), nodes.labels())
  }

  @Test
  fun testNoOp() {
    val nodes = nodes("A", "B", "C")
    nodes.move(from = 1, to = 1, count = 1)
    assertEquals(listOf("A", "B", "C"), nodes.labels())
  }
}

private fun nodes(vararg labels: String): RootNode =
  RootNode().apply { labels.forEach { children.add(TextNode(text = it)) } }

private fun RootNode.labels(): List<String> = children.map { (it as TextNode).text }
