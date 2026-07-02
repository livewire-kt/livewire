@file:OptIn(ExperimentalComposeRuntimeApi::class, ComposeToolingApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionImpl
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.RecomposerInfo
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.CompositionObserverHandle
import androidx.compose.runtime.tooling.CompositionRegistrationObserver
import androidx.compose.runtime.tooling.ObservableComposition
import co.touchlab.stately.collections.ConcurrentMutableMap
import com.r0adkll.livewire.ui.composition.LivewireRecomposers
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object RecompositionTracker {
  val version: StateFlow<Long>
    field = MutableStateFlow(0L)

  // copy on write so it can be read by the ui thread while being rebuilt by publish()
  @Volatile
  internal var roots: List<ComposableNode> = emptyList()
    private set

  private val registry = NodeRegistry()
  private val builder = CompositionTreeBuilder(registry)

  private val compositionRoots = mutableMapOf<ObservableComposition, List<ComposableNode>>()
  private var graftPoints = setOf<ComposableNode>()
  private var windowCounter = 0
  private val windowIndices = mutableMapOf<Recomposer, Int>()
  private val windowNodes = mutableMapOf<Recomposer, ComposableNode>()

  private val registrationHandles = mutableMapOf<RecomposerInfo, CompositionObserverHandle>()

  private val compositionObserverHandles = ConcurrentMutableMap<ObservableComposition, CompositionObserverHandle>()

  private val compositionScopes = ConcurrentMutableMap<ObservableComposition, MutableSet<RecomposeScope>>()

  private val captureJobs = ConcurrentMutableMap<ObservableComposition, MutableSet<Job>>()

  private val commands = Channel<Command>(Channel.UNLIMITED)
  private val publishSignal = Channel<Unit>(Channel.CONFLATED)

  @Volatile
  private var sourceInfoEnabled = false

  @Volatile
  private var started = false

  private var trackingScope: CoroutineScope? = null

  fun enableSourceInformation() {
    if (sourceInfoEnabled) return
    sourceInfoEnabled = true
    Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
  }

  fun start(scope: CoroutineScope = MainScope()) {
    if (started) return
    started = true

    enableSourceInformation()
    trackingScope = scope

    scope.launch {
      for (command in commands) process(command)
    }

    scope.launch {
      for (signal in publishSignal) {
        publish()
        delay(PublishThrottleMs)
      }
    }

    // on the main thread because the ios recomposer is a threadlocal
    scope.launch {
      val observed = mutableSetOf<Any>()
      Recomposer.runningRecomposers.collect { recomposers ->
        recomposers.forEach { if (observed.add(it)) track(it) }

        val removed = observed - recomposers
        removed.forEach {
          observed.remove(it)
          untrack(it)
        }
      }
    }
  }

  fun stop() {
    if (!started) return
    started = false

    trackingScope?.cancel()
    trackingScope = null

    registrationHandles.values.forEach { it.dispose() }
    registrationHandles.clear()

    compositionObserverHandles.block { handles ->
      handles.values.forEach { it.dispose() }
      handles.clear()
    }

    // drop work queued before the processor was canceled so the next start begins clean
    @Suppress("ControlFlowWithEmptyBody")
    while (commands.tryReceive().isSuccess) { }

    compositionRoots.clear()
    compositionScopes.block { it.clear() }
    captureJobs.block { it.clear() }
    graftPoints = emptySet()
    windowIndices.clear()
    windowNodes.clear()
    windowCounter = 0
    registry.clear()
    roots = emptyList()
    version.value++
  }

  private fun track(info: RecomposerInfo) {
    if (info in registrationHandles) return
    val handle = info.observe(registrationObserver) ?: return
    registrationHandles[info] = handle
  }

  private fun untrack(key: Any) {
    registrationHandles.remove(key)?.dispose()
  }

  internal fun snapshotRoots(): List<ComposableNode> = roots

  private val registrationObserver = object : CompositionRegistrationObserver {
    override fun onCompositionRegistered(composition: ObservableComposition) {
      if ((composition as? CompositionImpl)?.parent in LivewireRecomposers) return

      val handle = composition.setObserver(compositionObserverFor(composition))
      compositionObserverHandles.block { it[composition] = handle }
      commands.trySend(Command.Capture(composition, emptySet()))
    }

    override fun onCompositionUnregistered(composition: ObservableComposition) {
      compositionObserverHandles.remove(composition)?.dispose()
      compositionScopes.remove(composition)
      commands.trySend(Command.Unregister(composition))
    }
  }

  private fun compositionObserverFor(composition: ObservableComposition) = object : CompositionObserver {
    override fun onScopeEnter(scope: RecomposeScope) {
      compositionScopes.block { it.getOrPut(composition) { mutableSetOf() }.add(scope) }
    }

    override fun onEndComposition(composition: ObservableComposition) {
      val composedScopes = compositionScopes.remove(composition)?.toSet().orEmpty()
      commands.trySend(Command.Capture(composition, composedScopes))
    }

    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
      commands.trySend(Command.Invalidate(scope, value))
    }

    override fun onScopeDisposed(scope: RecomposeScope) {
      commands.trySend(Command.DisposeScope(scope))
    }

    override fun onBeginComposition(composition: ObservableComposition) = Unit
    override fun onScopeExit(scope: RecomposeScope) = Unit
    override fun onReadInScope(scope: RecomposeScope, value: Any) = Unit
  }

  private fun process(command: Command) {
    when (command) {
      is Command.Capture -> scheduleCapture(command.composition, command.composedScopes)
      is Command.Rebuild -> rebuild(command.composition, command.snapshot, command.composedScopes)
      is Command.Invalidate -> registry.nodeForScope(command.scope)?.recordInvalidation(command.value)
      is Command.DisposeScope -> registry.unbindScope(command.scope)
      is Command.Unregister -> {
        captureJobs.remove(command.composition)?.forEach { it.cancel() }
        compositionRoots.remove(command.composition)
        requestPublish()
      }
    }
  }

  // this is annoying and might have a better solution, but it works for now. so here's why we're doing this:
  // a composition's slot table is only written during its recomposer's frame dispatch, so reading it via withFrameNanos on the
  // compositions's own clock keeps us thread safe. it also keeps the read happening after `apply`
  private fun scheduleCapture(composition: ObservableComposition, composedScopes: Set<RecomposeScope>) {
    if (!compositionObserverHandles.containsKey(composition)) return // disposed while queued

    val scope = trackingScope
    val frameClock = try {
      (composition as? CompositionImpl)?.parent?.effectCoroutineContext[MonotonicFrameClock]
    } catch (_: Throwable) {
      null
    }

    if (scope == null || frameClock == null) {
      // no clock to use as a trampoline, which _SHOULD_ mean the composition is on the main thread and we can read here
      captureSnapshot(composition)?.let { rebuild(composition, it, composedScopes) }
      return
    }

    val job = scope.launch {
      val snapshot = frameClock.withFrameNanos { captureSnapshot(composition) }
      if (snapshot != null) {
        commands.trySend(Command.Rebuild(composition, snapshot, composedScopes))
      }
    }
    captureJobs.block { jobs -> jobs.getOrPut(composition) { mutableSetOf() }.add(job) }
    job.invokeOnCompletion {
      captureJobs.block { jobs ->
        val pending = jobs[composition] ?: return@block
        pending.remove(job)
        if (pending.isEmpty()) {
          jobs.remove(composition)
        }
      }
    }
  }

  private fun rebuild(
    composition: ObservableComposition,
    snapshot: List<GroupSnapshot>,
    composedScopes: Set<RecomposeScope>,
  ) {
    if (!compositionObserverHandles.containsKey(composition)) return
    compositionRoots[composition] = builder.build(snapshot, composedScopes)
    requestPublish()
  }

  private fun captureSnapshot(composition: ObservableComposition): List<GroupSnapshot>? {
    val compositionImpl = composition as? CompositionImpl ?: return null
    val compositionData = try {
      compositionImpl.slotStorage as CompositionData
    } catch (_: Throwable) {
      return null
    }
    return builder.snapshot(compositionData.compositionGroups)
  }

  private fun mergeRoots(): List<ComposableNode> {
    val rootsByRecomposer = LinkedHashMap<Recomposer, MutableList<ComposableNode>>()
    val subcompositions = mutableListOf<Pair<CompositionContext, List<ComposableNode>>>()

    compositionRoots.forEach { (composition, roots) ->
      val compositionImpl = composition as? CompositionImpl ?: return@forEach
      when (val parent = compositionImpl.parent) {
        is Recomposer -> rootsByRecomposer.getOrPut(parent) { mutableListOf() }.addAll(roots)
        else -> subcompositions.add(parent to roots)
      }
    }

    val graft = graftSubcompositions(subcompositions, graftPoints) { registry.graftPointFor(it) }
    graftPoints = graft.graftPoints
    val contentRecomposers = rootsByRecomposer.filterValues { !it.isDesktopWindowPlumbing() }

    windowIndices.keys.retainAll(contentRecomposers.keys)
    windowNodes.keys.retainAll(contentRecomposers.keys)

    return groupRootCompositions(contentRecomposers, graft.orphans, ::windowGroupNode)
  }

  private fun windowGroupNode(recomposer: Recomposer, roots: List<ComposableNode>): ComposableNode {
    val index = windowIndices.getOrPut(recomposer) { windowCounter++ }
    return windowNodes.getOrPut(recomposer) { ComposableNode(key = recomposer, name = "Window $index") }
      .also { it.setChildren(roots) }
  }

  private fun requestPublish() = publishSignal.trySend(Unit)

  private fun publish() {
    val newRoots = mergeRoots()
    registry.prune(newRoots)
    roots = newRoots
    version.value++
  }
}

private sealed interface Command {
  class Capture(
    val composition: ObservableComposition,
    val composedScopes: Set<RecomposeScope>,
  ) : Command
  class Rebuild(
    val composition: ObservableComposition,
    val snapshot: List<GroupSnapshot>,
    val composedScopes: Set<RecomposeScope>,
  ) : Command
  class Invalidate(val scope: RecomposeScope, val value: Any?) : Command
  class DisposeScope(val scope: RecomposeScope) : Command
  class Unregister(val composition: ObservableComposition) : Command
}

private const val PublishThrottleMs = 100L
