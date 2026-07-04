package com.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlattenTreeTest {
  @Test
  fun `emits depth rows for an expanded tree`() {
    val root = collapsableNode("Root", children = listOf(collapsableNode("A"), collapsableNode("B")))
    val rows = flattenTree(listOf(root), expandOverrides = emptyMap(), breadcrumbExpansions = emptyMap())

    assertEquals(listOf("Root", "A", "B"), rows.map { it.name })
    assertEquals(listOf(0, 1, 1), rows.map { it.depth })
  }

  @Test
  fun `hides the children of a collapsed node`() {
    val root = collapsableNode("Root", children = listOf(collapsableNode("A"), collapsableNode("B")))
    val rows = flattenTree(listOf(root), expandOverrides = mapOf("Root" to false), breadcrumbExpansions = emptyMap())

    assertEquals(listOf("Root"), rows.map { it.name })
  }

  @Test
  fun `reports if row has children`() {
    val root = collapsableNode("Root", children = listOf(collapsableNode("A")))
    val rows = flattenTree(listOf(root), expandOverrides = emptyMap(), breadcrumbExpansions = emptyMap())

    assertTrue(rows.first { it.name == "Root" }.hasChildren)
    assertEquals(false, rows.first { it.name == "A" }.hasChildren)
  }

  @Test
  fun `emits expanded breadcrumb as its own row`() {
    val leaf = collapsableNode(
      name = "Leaf",
      breadcrumbs = listOf(BreadcrumbEntry(name = "Mid", key = "midKey", parameters = emptyList())),
    )

    val rows = flattenTree(
      nodes = listOf(leaf),
      expandOverrides = emptyMap(),
      breadcrumbExpansions = mapOf("Leaf" to setOf(0)),
    )

    assertEquals(listOf("Mid", "Leaf"), rows.map { it.name })
    assertTrue(rows[0].isBreadcrumbRow)
    assertEquals(listOf(0, 1), rows.map { it.depth })
  }

  @Test
  fun `emits each expanded breadcrumb as its own row`() {
    val node = collapsableNode(
      name = "Leaf",
      breadcrumbs = listOf(
        BreadcrumbEntry(name = "A", key = "ka", parameters = emptyList()),
        BreadcrumbEntry(name = "B", key = "kb", parameters = emptyList()),
      ),
    )

    val rows = flattenTree(listOf(node), expandOverrides = emptyMap(), breadcrumbExpansions = mapOf("Leaf" to setOf(0, 1)))

    assertEquals(listOf("A", "B", "Leaf"), rows.map { it.name })
    assertEquals(listOf(0, 1, 2), rows.map { it.depth })
    assertTrue(rows[0].isBreadcrumbRow)
    assertTrue(rows[1].isBreadcrumbRow)
  }
}

private fun collapsableNode(
  name: String,
  key: Any = name,
  children: List<CollapsableNode> = emptyList(),
  breadcrumbs: List<BreadcrumbEntry> = emptyList(),
): CollapsableNode = CollapsableNode(
  name = name,
  breadcrumbs = breadcrumbs,
  recompositionCount = 0,
  skipCount = 0,
  childRecompositionCount = 0,
  children = children,
  key = key,
  invalidationReasons = emptyList(),
  parameters = emptyList(),
  recompositionRate = 0f,
)
