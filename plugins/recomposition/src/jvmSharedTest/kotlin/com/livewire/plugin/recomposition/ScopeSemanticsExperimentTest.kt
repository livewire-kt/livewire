@file:OptIn(ComposeToolingApi::class, ExperimentalComposeRuntimeApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.PausableComposition
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.tooling.ComposeToolingApi
import kotlin.test.Test
import kotlin.test.assertEquals

class ScopeSemanticsExperimentTest {
  private val executions = mutableMapOf<String, Int>()
  private fun ran(name: String) { executions[name] = (executions[name] ?: 0) + 1 }

  private val state = mutableIntStateOf(0)
  private val base = mutableIntStateOf(1)
  private val derived = derivedStateOf { base.value > 0 }

  @Composable
  private fun Root() {
    ran("Root")
    state.value
    Stable(value = 1)
  }

  @Composable
  private fun Stable(value: Int) {
    ran("Stable")
    Wrapper { Leaf() }
  }

  @Composable
  private inline fun Wrapper(content: @Composable () -> Unit) {
    ran("Wrapper")
    content()
  }

  @Composable
  private fun Leaf() {
    ran("Leaf")
  }

  @Composable
  private fun DerivedRoot() {
    ran("DerivedRoot")
    DerivedReader()
  }

  @Composable
  private fun DerivedReader() {
    ran("DerivedReader")
    derived.value
  }

  @Composable
  private fun PausedRoot() {
    ran("PausedRoot")
    Leaf()
    Leaf()
    Leaf()
  }

  @Test
  fun pausedScopesAreNotCountedUntilTheyRun() {
    val harness = RecomposeHarness()
    val observer = RecordingObserver()
    val composition = PausableComposition(NoopApplier(), harness.recomposer)
    composition.observe(observer)
    executions.clear()

    val paused = composition.setPausableContent { PausedRoot() }
    var checks = 0
    while (!paused.isComplete) {
      paused.resume { checks++ % 2 == 0 }
    }
    paused.apply()

    println("truth: $executions")
    println("events: ${observer.events.describe()}")

    val tree = CompositionTreeBuilder(NodeRegistry()).build(composition.compositionData().compositionGroups, observer.pass())!!
    println("tree:\n${tree.dump()}")

    assertEquals(mapOf("PausedRoot" to 1, "Leaf" to 3), executions)
    tree.flatten().filter { it.name == "Leaf" || it.name == "PausedRoot" }.forEach { assertEquals(1, it.compositionCount, it.name) }
    harness.close()
  }

  @Test
  fun parentRecomposesChildSkips() {
    val harness = RecomposeHarness()
    val observer = RecordingObserver()
    val composition = harness.compose { Root() }
    composition.observe(observer)
    observer.clear()
    executions.clear()

    state.value++
    harness.frame()

    println("truth: $executions")
    println("events: ${observer.events.describe()}")

    val builder = CompositionTreeBuilder(NodeRegistry())
    val byEntered = builder.build(composition.compositionData().compositionGroups, ScopePass(observer.entered(), emptySet()))!!
    println("tree using entered scopes:\n${byEntered.dump()}")
    val byExecuted = CompositionTreeBuilder(NodeRegistry()).build(composition.compositionData().compositionGroups, observer.pass())!!
    println("tree using executed scopes:\n${byExecuted.dump()}")

    assertEquals(mapOf("Root" to 1), executions)
    harness.close()
  }

  @Test
  fun derivedStateChangedExecutes() {
    val harness = RecomposeHarness()
    val observer = RecordingObserver()
    val composition = harness.compose { DerivedRoot() }
    composition.observe(observer)
    observer.clear()
    executions.clear()

    base.value = -1
    harness.frame()

    println("truth: $executions")
    println("events: ${observer.events.describe()}")
    val byExecuted = CompositionTreeBuilder(NodeRegistry()).build(composition.compositionData().compositionGroups, observer.pass())!!
    println("tree using executed scopes:\n${byExecuted.dump()}")

    assertEquals(mapOf("DerivedReader" to 1), executions)
    assertEquals(1, byExecuted.flatten().first { it.name == "DerivedReader" }.compositionCount)
    harness.close()
  }

  @Test
  fun derivedStateUnchangedRereadsScopeWithoutExecuting() {
    val harness = RecomposeHarness()
    val observer = RecordingObserver()
    val composition = harness.compose { DerivedRoot() }
    composition.observe(observer)
    observer.clear()
    executions.clear()

    base.value = 2
    harness.frame()

    println("truth: $executions")
    println("events: ${observer.events.describe()}")
    println("begins=${observer.begins} ends=${observer.ends}")

    val byEntered = CompositionTreeBuilder(NodeRegistry()).build(composition.compositionData().compositionGroups, ScopePass(observer.entered(), emptySet()))!!
    println("tree using entered scopes:\n${byEntered.dump()}")

    val byExecuted = CompositionTreeBuilder(NodeRegistry()).build(composition.compositionData().compositionGroups, observer.pass())!!
    println("tree using executed scopes:\n${byExecuted.dump()}")

    assertEquals(emptyMap(), executions)
    assertEquals(0, byExecuted.flatten().first { it.name == "DerivedReader" }.compositionCount)
    harness.close()
  }
}

private fun List<ScopeEvent>.describe() = map {
  when (it) {
    is ScopeEvent.Enter -> "Enter(willExecute=${it.willExecute})"
    is ScopeEvent.Exit -> "Exit(skipped=${it.skipped}${if (it.paused) ", paused" else ""})"
    is ScopeEvent.Invalidated -> "Invalidated(${it.value?.let { v -> v::class.simpleName }})"
    is ScopeEvent.Disposed -> "Disposed"
  }
}
