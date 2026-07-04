package com.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CompositionForestTest {
  @Test
  fun `grafts subcomposition onto parent`() {
    val root = node("Root")
    val sub = node("Sub")

    val result = graftSubcompositions(listOf("context" to listOf(sub)), emptySet()) { context ->
      if (context == "context") root else null
    }

    assertEquals(emptyList(), result.orphans)
    assertEquals(setOf(root), result.graftPoints)
    assertEquals(listOf(sub), root.children)
  }

  @Test
  fun `returns orphan if there are no graft points`() {
    val sub = node("Sub")
    val result = graftSubcompositions(listOf("missing" to listOf(sub)), emptySet()) { null }

    assertEquals(listOf(sub), result.orphans)
    assertEquals(emptySet(), result.graftPoints)
  }

  @Test
  fun `regrafting replaces instead of adding`() {
    val root = node("Root")
    val sub = node("Sub")

    graftSubcompositions(listOf("context" to listOf(sub)), emptySet()) { root }
    graftSubcompositions(listOf("context" to listOf(sub)), setOf(root)) { root }

    assertEquals(1, root.children.size)
    assertSame(sub, root.children.single())
  }

  @Test
  fun `grafted children survive rebuilds`() {
    val root = node("Root")
    val own = node("Own")
    val sub = node("Sub")

    graftSubcompositions(listOf("context" to listOf(sub)), emptySet()) { root }
    root.setChildren(listOf(own))

    assertEquals(listOf(own, sub), root.children)
  }

  @Test
  fun `node that loses its subcomposition is cleared`() {
    val root = node("Root")
    val sub = node("Sub")

    graftSubcompositions(listOf("context" to listOf(sub)), emptySet()) { root }
    val result = graftSubcompositions(emptyList<Pair<String, List<ComposableNode>>>(), setOf(root)) { null }

    assertEquals(emptySet(), result.graftPoints)
    assertEquals(emptyList(), root.children)
  }

  @Test
  fun `multiple subcompositions grafted onto one point`() {
    val root = node("Root")
    val sub1 = node("Dup")
    val sub2 = node("Dup")

    graftSubcompositions(listOf("a" to listOf(sub1), "b" to listOf(sub2)), emptySet()) { root }

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
