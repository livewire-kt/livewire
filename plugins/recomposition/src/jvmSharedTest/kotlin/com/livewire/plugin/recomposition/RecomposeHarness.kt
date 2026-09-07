@file:OptIn(ComposeToolingApi::class, ExperimentalComposeRuntimeApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.collection.MutableScatterSet
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionImpl
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.RecomposeScopeImpl
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.ObservableComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class RecomposeHarness {
  val clock = BroadcastFrameClock()
  private val job = Job()
  val scope = CoroutineScope(Dispatchers.Unconfined + clock + job)
  val recomposer = Recomposer(scope.coroutineContext)
  private var frameTime = 0L

  init {
    Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
    scope.launch { recomposer.runRecomposeAndApplyChanges() }
  }

  fun compose(content: @Composable () -> Unit): Composition {
    val composition = Composition(NoopApplier(), recomposer)
    composition.setContent(content)
    return composition
  }

  fun frame() {
    Snapshot.sendApplyNotifications()
    frameTime += 16_000_000L
    clock.sendFrame(frameTime)
  }

  fun close() {
    recomposer.cancel()
    scope.cancel()
  }
}

internal class NoopApplier : AbstractApplier<Any?>(null) {
  override fun insertTopDown(index: Int, instance: Any?) = Unit
  override fun insertBottomUp(index: Int, instance: Any?) = Unit
  override fun remove(index: Int, count: Int) = Unit
  override fun move(from: Int, to: Int, count: Int) = Unit
  override fun onClear() = Unit
}

internal sealed interface ScopeEvent {
  val scope: RecomposeScope

  data class Enter(override val scope: RecomposeScope, val willExecute: Boolean) : ScopeEvent
  data class Exit(override val scope: RecomposeScope, val skipped: Boolean, val paused: Boolean = false) : ScopeEvent
  data class Invalidated(override val scope: RecomposeScope, val value: Any?) : ScopeEvent
  data class Disposed(override val scope: RecomposeScope) : ScopeEvent
}

internal class RecordingObserver : CompositionObserver {
  val events = mutableListOf<ScopeEvent>()
  var begins = 0
  var ends = 0

  override fun onBeginComposition(composition: ObservableComposition) {
    begins++
  }

  override fun onEndComposition(composition: ObservableComposition) {
    ends++
  }

  private val pending = HashMap<RecomposeScope, MutableScatterSet<Any>?>()

  override fun onScopeEnter(scope: RecomposeScope) {
    val willExecute = if (scope in pending) {
      val instances = pending.remove(scope)
      instances == null || (scope as RecomposeScopeImpl).isInvalidFor(instances)
    } else {
      true
    }
    events += ScopeEvent.Enter(scope, willExecute)
  }

  override fun onScopeExit(scope: RecomposeScope) {
    scope as RecomposeScopeImpl
    events += ScopeEvent.Exit(scope, skipped = scope.skipped, paused = scope.paused)
  }

  override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
    if (value == null) {
      pending[scope] = null
    } else if (scope !in pending) {
      pending[scope] = MutableScatterSet<Any>().also { it.add(value) }
    } else {
      pending[scope]?.add(value)
    }
    events += ScopeEvent.Invalidated(scope, value)
  }

  override fun onScopeDisposed(scope: RecomposeScope) {
    events += ScopeEvent.Disposed(scope)
  }

  override fun onReadInScope(scope: RecomposeScope, value: Any) = Unit

  fun entered(): Set<RecomposeScope> = events.filterIsInstance<ScopeEvent.Enter>().map { it.scope }.toSet()
  fun executed(): Set<RecomposeScope> {
    val rereads = events.filterIsInstance<ScopeEvent.Enter>().filter { !it.willExecute }.map { it.scope }.toSet()
    return events.filterIsInstance<ScopeEvent.Exit>().filter { !it.skipped && !it.paused && it.scope !in rereads }.map { it.scope }.toSet()
  }

  fun skipped(): Set<RecomposeScope> = events.filterIsInstance<ScopeEvent.Exit>().filter { it.skipped }.map { it.scope }.toSet()
  fun paused(): Set<RecomposeScope> = events.filterIsInstance<ScopeEvent.Exit>().filter { it.paused }.map { it.scope }.toSet()
  fun pass() = ScopePass(executed(), skipped(), paused())
  fun clear() {
    events.clear()
    begins = 0
    ends = 0
  }
}

internal fun Composition.compositionData(): CompositionData = (this as CompositionImpl).slotStorage as CompositionData

internal fun Composition.observe(observer: CompositionObserver) = (this as ObservableComposition).setObserver(observer)

internal fun List<ComposableNode>.flatten(): List<ComposableNode> = flatMap { listOf(it) + it.children.flatten() }

internal fun List<ComposableNode>.dump(indent: String = ""): String = joinToString("") { node ->
  "$indent${node.name} comps=${node.compositionCount} skips=${node.skipCount}\n" + node.children.dump("$indent  ")
}
