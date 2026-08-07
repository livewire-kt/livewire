package com.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HiddenChainsTest {

  @Test
  fun `hides the hot reload spacer chain`() {
    assertEquals(emptyList(), overlayContent(overlay(effectVisibility())).children)
  }

  @Test
  fun `keeps siblings of a hidden chain`() {
    val content = overlayContent(overlay(composable("LeakAlertOverlay"), effectVisibility(), composable("Content")))

    assertEquals(listOf("LeakAlertOverlay", "Content"), content.children.map { it.name })
  }

  @Test
  fun `hides the chain when a link has extra children`() {
    val chain = composable(
      name = "EffectVisibility",
      children = listOf(
        composable(
          name = "Box",
          children = listOf(
            composable("Sibling"),
            composable("Layout", children = listOf(composable("Spacer"), composable("Text"))),
          ),
        ),
      ),
    )

    assertEquals(emptyList(), overlayContent(overlay(chain)).children)
  }

  @Test
  fun `keeps the chain when there is no ReloadEffects in the parent path`() {
    val roots = build(
      composable(
        name = "App",
        children = listOf(composable("OverlayLayout", children = listOf(composable("Layout", children = listOf(effectVisibility()))))),
      ),
    )

    assertNotNull(roots.first().find("Spacer"))
  }

  @Test
  fun `hides every matching chain under the same parent`() {
    assertNull(build(overlay(effectVisibility("first"), effectVisibility("second"))).first().find("EffectVisibility"))
  }

  private fun build(root: FakeGroup) = CompositionTreeBuilder(NodeRegistry()).build(listOf(root), emptySet())!!

  private fun path(vararg names: String, identity: String = names.first()): FakeGroup =
    if (names.size == 1) {
      composable(names.first(), identity = "$identity.${names.first()}")
    } else {
      composable(
        name = names.first(),
        identity = "$identity.${names.first()}",
        children = listOf(path(*names.drop(1).toTypedArray(), identity = "$identity.${names.first()}")),
      )
    }

  private fun overlay(vararg children: FakeGroup) = composable(
    name = "DevelopmentEntryPoint",
    children = listOf(
      composable(
        name = "ReloadEffects",
        children = listOf(composable("OverlayLayout", children = listOf(composable("Layout", children = children.toList())))),
      ),
    ),
  )

  private fun overlayContent(root: FakeGroup) = build(root).find("OverlayLayout").children.single()

  private fun effectVisibility(identity: String = "chain") =
    path("EffectVisibility", "Box", "Layout", "Spacer", "Layout", identity = identity)
}
