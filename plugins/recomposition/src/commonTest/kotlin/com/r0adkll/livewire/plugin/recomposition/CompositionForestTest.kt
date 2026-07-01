package com.r0adkll.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CompositionForestTest {
  @Test
  fun `graphs subcomposition onto parent`() {
    val root = node("Root")
    val sub = node("Sub")

    val orphans = graftSubcompositions(listOf("context" to listOf(sub))) { context ->
      if (context == "context") root else null
    }

    assertEquals(emptyList(), orphans)
    assertEquals(listOf(sub), root.children)
  }

  @Test
  fun `returns orphan if there are no graft points`() {
    val sub = node("Sub")
    val orphans = graftSubcompositions(listOf("missing" to listOf(sub))) { null }

    assertEquals(listOf(sub), orphans)
  }

  @Test
  fun `doesn't duplicate attached child`() {
    val root = node("Root")
    val sub = node("Sub")
    root.addChild(sub)

    graftSubcompositions(listOf("context" to listOf(sub))) { root }

    assertEquals(1, root.children.size)
    assertSame(sub, root.children.single())
  }

  @Test
  fun `graft uses reference id`() {
    val root = node("Root")
    val sub1 = node("Dup")
    val sub2 = node("Dup")
    root.addChild(sub1)

    graftSubcompositions(listOf("context" to listOf(sub2))) { root }

    assertEquals(2, root.children.size)
  }

  @Test
  fun `single recomposer stsays flat`() {
    val a = node("A")
    val b = node("B")

    val result = groupRootCompositions(mapOf("only" to listOf(a, b)), orphans = emptyList()) { _, _ ->
      error("shouldn't wrap a single group")
    }

    assertEquals(listOf(a, b), result)
  }

  @Test
  fun `multiple recomposes wrapped per group`() {
    val a = node("A")
    val b = node("B")

    val result = groupRootCompositions(
      linkedMapOf("g1" to listOf(a), "g2" to listOf(b)),
      orphans = emptyList(),
    ) { g, roots -> group("Window-$g", roots) }

    assertEquals(listOf("Window-g1", "Window-g2"), result.map { it.name })
    assertEquals(listOf(a), result[0].children)
    assertEquals(listOf(b), result[1].children)
  }

  @Test
  fun `orphans appended after groups`() {
    val orphan = node("Orphan")

    val result = groupRootCompositions(
      linkedMapOf("g1" to listOf(node("A")), "g2" to listOf(node("B"))),
      orphans = listOf(orphan),
    ) { g, roots -> group("Window-$g", roots) }

    assertEquals(3, result.size)
    assertSame(orphan, result.last())
  }

  @Test
  fun `window plumbing stuff works`() {
    assertTrue(emptyList<ComposableNode>().isDesktopWindowPlumbing())
    assertTrue(listOf(node("Window")).isDesktopWindowPlumbing())
    assertTrue(listOf(node("Window"), node("Tray")).isDesktopWindowPlumbing())
    assertFalse(listOf(node("Window"), node("Content")).isDesktopWindowPlumbing())
    assertFalse(listOf(node("Layout")).isDesktopWindowPlumbing())
  }

  @Test
  fun `orphans appended for single group`() {
    val a = node("A")
    val orphan = node("Orphan")

    val result = groupRootCompositions(mapOf("only" to listOf(a)), orphans = listOf(orphan)) { _, _ ->
      error("shouldn't wrap a single group")
    }

    assertEquals(listOf(a, orphan), result)
  }
}

private fun node(name: String) = ComposableNode(name, name)

private fun group(name: String, roots: List<ComposableNode>) = ComposableNode(name, name).also { it.setChildren(roots) }
