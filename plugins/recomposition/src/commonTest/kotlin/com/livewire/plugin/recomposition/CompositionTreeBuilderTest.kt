package com.livewire.plugin.recomposition

import androidx.compose.runtime.Composer
import androidx.compose.runtime.RecomposeScope
import kotlin.test.Test
import kotlin.test.assertEquals

class CompositionTreeBuilderTest {
  private val registry = NodeRegistry()
  private val builder = CompositionTreeBuilder(registry)

  private val appScope = FakeScope()
  private val aScope = FakeScope()
  private val bScope = FakeScope()

  private val groups = listOf(
    composable(
      name = "App",
      scope = appScope,
      children = listOf(
        composable("A", aScope),
        composable("B", bScope),
      ),
    ),
  )

  private fun build(vararg executed: RecomposeScope, skipped: Set<RecomposeScope> = emptySet()) =
    builder.build(groups, ScopePass(executed.toSet(), skipped))!!

  @Test
  fun `counts every reached node on initial composition`() {
    val roots = build(appScope, aScope, bScope)

    listOf("App", "A", "B").forEach { name ->
      val node = roots.find(name)
      assertEquals(1, node.compositionCount, "$name compositionCount")
      assertEquals(0, node.recompositionCount, "$name recompositionCount")
      assertEquals(0, node.skipCount, "$name skipCount")
    }
  }

  @Test
  fun `counts skipped children as skips when parent recomposes`() {
    build(appScope, aScope, bScope)
    val roots = build(appScope, skipped = setOf(aScope, bScope))

    assertEquals(2, roots.find("App").compositionCount)
    assertEquals(1, roots.find("App").recompositionCount)

    listOf("A", "B").forEach { name ->
      val node = roots.find(name)
      assertEquals(1, node.skipCount, "$name skipCount")
      assertEquals(0, node.recompositionCount, "$name recompositionCount")
    }
  }

  @Test
  fun `doesn't count retained but untouched nodes as skips`() {
    build(appScope, aScope, bScope)
    build(appScope, skipped = setOf(aScope, bScope))
    val roots = build(aScope)

    assertEquals(2, roots.find("A").compositionCount)
    assertEquals(1, roots.find("A").recompositionCount)
    assertEquals(2, roots.find("App").compositionCount)
    assertEquals(0, roots.find("App").skipCount)
    assertEquals(1, roots.find("B").compositionCount)
    assertEquals(1, roots.find("B").skipCount)
  }

  @Test
  fun `counts an unscoped named node as a composition when reached`() {
    val tree = listOf(
      composable(
        name = "App", appScope,
        children = listOf(composable("Inline", scope = null)),
      ),
    )

    val roots = CompositionTreeBuilder(NodeRegistry()).build(tree, ScopePass(setOf(appScope), emptySet()))!!

    val inline = roots.find("Inline")
    assertEquals(1, inline.compositionCount)
    assertEquals(0, inline.skipCount)
  }

  @Test
  fun `reaches a leaf in a transparent group`() {
    val leafScope = FakeScope()
    val tree = listOf(
      composable(
        name = "App", appScope,
        children = listOf(
          transparent("t", children = listOf(composable("Leaf", leafScope))),
        ),
      ),
    )
    val builder = CompositionTreeBuilder(NodeRegistry())

    assertEquals(1, builder.build(tree, ScopePass(setOf(appScope, leafScope), emptySet()))!!.find("Leaf").compositionCount)
  }

  @Test
  fun `node under an unexecuted subtree isn't composed or skipped`() {
    val leafScope = FakeScope()
    val tree = listOf(
      composable("App", appScope, children = listOf(composable("Leaf", leafScope))),
    )

    val roots = CompositionTreeBuilder(NodeRegistry()).build(tree, ScopePass.Empty)!!

    val leaf = roots.find("Leaf")
    assertEquals(0, leaf.compositionCount)
    assertEquals(0, leaf.skipCount)
  }

  @Test
  fun `a scope entered but skipped does not compose its inline children`() {
    val tree = listOf(
      composable(
        name = "App", appScope,
        children = listOf(
          composable("Stable", aScope, children = listOf(composable("Inline", scope = null))),
        ),
      ),
    )
    val builder = CompositionTreeBuilder(NodeRegistry())
    builder.build(tree, ScopePass(setOf(appScope, aScope), emptySet()))
    val roots = builder.build(tree, ScopePass(setOf(appScope), setOf(aScope)))!!

    assertEquals(1, roots.find("Stable").compositionCount)
    assertEquals(1, roots.find("Stable").skipCount)
    assertEquals(1, roots.find("Inline").compositionCount)
    assertEquals(0, roots.find("Inline").skipCount)
  }

  @Test
  fun `first execution of a node that predates tracking counts as a recomposition`() {
    build()
    val roots = build(appScope, skipped = setOf(aScope, bScope))

    assertEquals(1, roots.find("App").compositionCount)
    assertEquals(1, roots.find("App").recompositionCount)
    assertEquals(0, roots.find("A").compositionCount)
    assertEquals(1, roots.find("A").skipCount)
  }

  @Test
  fun `a parked page whose scope was released is flagged deactivated and hidden`() {
    val page = FakeGroup(
      key = "page",
      sourceInfo = "C(CharactersScreen)",
      data = listOf(Composer.Empty, 3),
      identity = "page",
      compositionGroups = listOf(composable("CharacterCard", scope = null)),
    )
    val tree = listOf(composable("Pager", appScope, children = listOf(page)))

    val roots = CompositionTreeBuilder(NodeRegistry()).build(tree, ScopePass(setOf(appScope), emptySet()))!!

    assertEquals(true, roots.find("CharactersScreen").deactivated)
    assertEquals(listOf<String>(), collapse(roots).single().children.map { it.name })
  }

  @Test
  fun `node tree matches hierarchy`() {
    val roots = build(appScope, aScope, bScope)
    assertEquals(1, roots.size)
    val app = roots.find("App")
    assertEquals(listOf("A", "B"), app.children.map { it.name })
  }
}
