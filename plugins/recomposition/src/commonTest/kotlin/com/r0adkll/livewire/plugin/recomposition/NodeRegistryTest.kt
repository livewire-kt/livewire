package com.r0adkll.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class NodeRegistryTest {
  @Test
  fun `reuses the same node instance for same identity`() {
    val registry = NodeRegistry()
    val first = registry.obtain("id", "Name")
    val second = registry.obtain("id", "Name")
    assertSame(first, second)
  }

  @Test
  fun `drops identities that are no longer present`() {
    val registry = NodeRegistry()
    val root = registry.obtain("root", "Root")
    val child = registry.obtain("child", "Child")
    registry.obtain("gone", "Gone")
    root.setChildren(listOf(child))

    registry.prune(listOf(root))

    assertEquals(setOf("root", "child"), registry.trackedIdentities())
  }

  @Test
  fun `keeps deeply nested nodes when pruning`() {
    val registry = NodeRegistry()
    val root = registry.obtain("root", "Root")
    val mid = registry.obtain("mid", "Mid")
    val leaf = registry.obtain("leaf", "Leaf")
    mid.setChildren(listOf(leaf))
    root.setChildren(listOf(mid))

    registry.prune(listOf(root))

    assertEquals(setOf("root", "mid", "leaf"), registry.trackedIdentities())
  }
}
