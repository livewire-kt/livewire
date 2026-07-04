package com.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposableNodeTest {
  @Test
  fun `initializes a freshly created node with counts of 0`() {
    val node = ComposableNode("k", "N")
    assertEquals(0, node.compositionCount)
    assertEquals(0, node.recompositionCount)
    assertEquals(0, node.skipCount)
  }

  @Test
  fun `first composition don't count as a recomposition`() {
    val node = ComposableNode("k", "N")
    node.recordEnter()
    node.recordComposition()
    assertEquals(1, node.compositionCount)
    assertEquals(0, node.recompositionCount)
    assertEquals(0, node.skipCount)
  }

  @Test
  fun `counts the second composition as a recomposition`() {
    val node = ComposableNode("k", "N")
    repeat(2) { node.recordEnter(); node.recordComposition() }
    assertEquals(2, node.compositionCount)
    assertEquals(1, node.recompositionCount)
  }

  @Test
  fun `counts an enter without a composition as a skip`() {
    val node = ComposableNode("k", "N")
    node.recordEnter(); node.recordComposition()
    node.recordEnter()
    node.recordEnter()
    assertEquals(2, node.skipCount)
    assertEquals(1, node.compositionCount)
  }

  @Test
  fun `records an unknown invalidation reason when recomposing without a known invalidation`() {
    val node = ComposableNode("k", "N")
    node.recordEnter(); node.recordComposition()
    assertTrue(node.recentInvalidationReasons().isEmpty())

    node.recordEnter(); node.recordComposition()
    val reasons = node.recentInvalidationReasons()
    assertEquals(1, reasons.size)
    assertTrue(reasons.last() is InvalidationReason.Unknown)
  }

  @Test
  fun `suppresses the unknown reason when an invalidation was observed before recomposing`() {
    val node = ComposableNode("k", "N")
    node.recordEnter(); node.recordComposition()
    node.recordInvalidation("trigger")
    node.recordEnter(); node.recordComposition()

    val reasons = node.recentInvalidationReasons()
    assertTrue(reasons.any { it is InvalidationReason.Reason })
    assertFalse(reasons.any { it is InvalidationReason.Unknown })
  }

  @Test
  fun `records a null invalidation value as a direct invalidation`() {
    val node = ComposableNode("k", "N")
    node.recordInvalidation(null)
    assertTrue(node.recentInvalidationReasons().single() is InvalidationReason.Direct)
  }

  @Test
  fun `keep only the three most recent invalidation reasons`() {
    val node = ComposableNode("k", "N")
    repeat(5) { node.recordInvalidation("v$it") }
    val reasons = node.recentInvalidationReasons()
    assertEquals(3, reasons.size)
    assertEquals(listOf("\"v2\"", "\"v3\"", "\"v4\""), reasons.map { it.value })
  }
}
