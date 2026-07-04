package com.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollapseTest {
  @Test
  fun `sums the recomposition counts of all displayed descendants`() {
    val aRecompositions = 1
    val bRecompositions = 4
    val cRecompositions = 2
    val dRecompositions = 3

    val aNode = node(name = "a", recompositions = aRecompositions)
    val bNode = node(name = "b", recompositions = bRecompositions)
    val cNode = node(name = "c", recompositions = cRecompositions)
    val dNode = node(name = "d", recompositions = dRecompositions, children = listOf(aNode, bNode))

    val root = node(name = "root", children = listOf(cNode, dNode))

    val collapsed = collapse(listOf(root)).single()
    assertEquals(aRecompositions + bRecompositions + cRecompositions + dRecompositions, collapsed.childRecompositionCount)
    assertEquals(aRecompositions + bRecompositions, collapsed.children.first { it.name == "d" }.childRecompositionCount)
    assertEquals(0, collapsed.children.first { it.name == "c" }.childRecompositionCount)
  }

  @Test
  fun `excludes hoisted uninteresting containers from the child recomposition count`() {
    val root = node(
      name = "root",
      children = listOf(
        node("outer", recompositions = 5, children = listOf(node("inner", recompositions = 2))),
        node("sibling", recompositions = 1),
      ),
    )

    val collapsed = collapse(listOf(root)).single()
    assertEquals(2 + 1, collapsed.childRecompositionCount)
  }

  @Test
  fun `reports a child recomposition count of 0 for displayed leaf`() {
    val root = node("root", children = listOf(node("leaf", recompositions = 4), node("other")))
    val collapsed = collapse(listOf(root)).single()
    assertEquals(0, collapsed.children.first { it.name == "leaf" }.childRecompositionCount)
  }

  @Test
  fun `collapses a single-child chain into breadcrumb entries`() {
    val root = node("root", children = listOf(node("middle", children = listOf(node("leaf")))))

    val collapsed = collapse(listOf(root))

    assertEquals("root", collapsed[0].name)
    val child = collapsed[0].children.single()
    assertEquals("leaf", child.name)
    assertEquals(listOf("middle"), child.breadcrumbs.map { it.name })
  }

  @Test
  fun `hoists the children of an uninteresting container into its parent`() {
    val root = node(
      name = "root",
      children = listOf(
        node("SubcomposeLayout", children = listOf(node("leaf1"), node("leaf2"))),
      ),
    )

    val names = collapse(listOf(root)).allNames()

    assertFalse("SubcomposeLayout" in names)
    assertTrue("leaf1" in names)
    assertTrue("leaf2" in names)
  }

  @Test
  fun `absorbs nested Layout nodes into their surrounding node`() {
    val root = node(name = "root", children = listOf(node("Box", children = listOf(node("Layout", children = listOf(node("inner")))))))

    val names = collapse(listOf(root)).allNames()

    assertFalse("Layout" in names)
    assertTrue("inner" in names)
  }

  @Test
  fun `absorbs a child into its parent when they have the same name`() {
    val root = node(
      name = "Root",
      children = listOf(
        node(
          name = "MaterialTheme",
          key = "m1",
          children = listOf(
            node("MaterialTheme", key = "m2", children = listOf(node("c1"), node("c2"))),
          ),
        ),
      ),
    )

    val names = collapse(listOf(root)).allNames()

    assertEquals(1, names.count { it == "MaterialTheme" })
    assertTrue("c1" in names)
    assertTrue("c2" in names)
  }

  @Test
  fun `keep a node's invalidation reasons when collapsing the tree`() {
    val child = ComposableNode(key = "child", name = "Child")
    child.recordEnter(); child.recordComposition()
    child.recordInvalidation("changed")
    child.recordEnter(); child.recordComposition()
    val root = node("Root", children = listOf(child.also { it.setChildren(emptyList()) }, node("Sibling")))

    val collapsedChild = collapse(listOf(root)).single().children.first { it.name == "Child" }

    assertTrue(collapsedChild.invalidationReasons.any { it is InvalidationReason.Reason })
  }
}

private fun node(
  name: String,
  key: Any = name,
  recompositions: Int = 0,
  children: List<ComposableNode> = emptyList(),
): ComposableNode {
  val node = ComposableNode(key, name)
  repeat(recompositions + 1) { node.recordEnter(); node.recordComposition() }
  node.setChildren(children)
  return node
}

private fun List<CollapsableNode>.allNames(): List<String> = flatMap { listOf(it.name) + it.children.allNames() }
