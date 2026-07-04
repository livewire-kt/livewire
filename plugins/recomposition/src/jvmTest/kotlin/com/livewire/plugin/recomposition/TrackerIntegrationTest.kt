@file:OptIn(ComposeToolingApi::class, ExperimentalComposeRuntimeApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionImpl
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.ObservableComposition
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackerIntegrationTest {
  @Test
  fun `test from a real composition`() {
    Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)

    val composedScopes = mutableSetOf<RecomposeScope>()
    val observer = object : CompositionObserver {
      override fun onScopeEnter(scope: RecomposeScope) {
        composedScopes.add(scope)
      }
      override fun onBeginComposition(composition: ObservableComposition) = Unit
      override fun onEndComposition(composition: ObservableComposition) = Unit
      override fun onScopeExit(scope: RecomposeScope) = Unit
      override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) = Unit
      override fun onScopeDisposed(scope: RecomposeScope) = Unit
      override fun onReadInScope(scope: RecomposeScope, value: Any) = Unit
    }

    val applier = object : AbstractApplier<Any?>(null) {
      override fun insertTopDown(index: Int, instance: Any?) = Unit
      override fun insertBottomUp(index: Int, instance: Any?) = Unit
      override fun remove(index: Int, count: Int) = Unit
      override fun move(from: Int, to: Int, count: Int) = Unit
      override fun onClear() = Unit
    }

    val composition = Composition(applier, Recomposer(Job()))
    (composition as ObservableComposition).setObserver(observer)
    composition.setContent { Root() }

    val data = (composition as CompositionImpl).slotStorage as CompositionData
    val roots = CompositionTreeBuilder(NodeRegistry()).build(data.compositionGroups, composedScopes)!!

    val root = roots.find("Root")
    val all = descendants(root)

    assertEquals(1, root.compositionCount)
    assertEquals(0, root.recompositionCount)
    assertEquals(2, all.count { it.name == "Branch" })
    assertEquals(2, all.count { it.name == "Leaf" })
    all.forEach { assertEquals(1, it.compositionCount, "${it.name} compositionCount") }
    all.forEach { assertEquals(0, it.skipCount, "${it.name} skipCount") }
  }
}

private fun descendants(node: ComposableNode): List<ComposableNode> = listOf(node) + node.children.flatMap { descendants(it) }

private val integrationState = mutableStateOf(0)

@Composable
private fun Leaf() {
  @Suppress("UNUSED_EXPRESSION")
  integrationState.value
}

@Composable
private fun Branch() {
  Leaf()
}

@Composable
private fun Root() {
  Branch()
  Branch()
}
