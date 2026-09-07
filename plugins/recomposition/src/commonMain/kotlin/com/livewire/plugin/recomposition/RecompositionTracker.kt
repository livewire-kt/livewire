@file:OptIn(ExperimentalComposeRuntimeApi::class, ComposeToolingApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionImpl
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.RecomposeScopeImpl
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.RecomposerInfo
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.CompositionObserverHandle
import androidx.compose.runtime.tooling.CompositionRegistrationObserver
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.collection.MutableScatterSet
import co.touchlab.stately.collections.ConcurrentMutableMap
import com.livewire.logError
import com.livewire.ui.composition.LivewireComposition
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
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

  private val passRecords = ConcurrentMutableMap<ObservableComposition, PassRecord>()

  // scopes invalidated since they were last entered, so entry can tell a real recomposition from a derived state reread
  private val pendingInvalidations = ConcurrentMutableMap<RecomposeScope, Any>()

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
    // child scope so stop() never cancels a scope the caller's still using
    val tracking = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job]))
    trackingScope = tracking

    tracking.launch {
      for (command in commands) {
        try {
          process(command)
        } catch (e: CancellationException) {
          throw e
        } catch (t: Throwable) {
          logError("Recomposition", "recomposition tracker failed while processing $command", t)
        }
      }
    }

    tracking.launch {
      for (signal in publishSignal) {
        try {
          publish()
        } catch (e: CancellationException) {
          throw e
        } catch (t: Throwable) {
          logError("Recomposition", "recomposition tracker failed while publishing", t)
        }
        delay(PublishThrottleMs)
      }
    }

    // on the main thread because the ios recomposer is a threadlocal
    tracking.launch {
      val observed = mutableSetOf<Any>()
      Recomposer.runningRecomposers.collect { recomposers ->
        recomposers.forEach {
          if (observed.add(it)) {
            try {
              track(it)
            } catch (e: CancellationException) {
              throw e
            } catch (t: Throwable) {
              logError("Recomposition", "recomposition tracker failed while observing recomposer", t)
            }

          }
        }

        val removed = observed - recomposers
        removed.forEach {
          observed.remove(it)
          try {
            untrack(it)
          } catch (e: CancellationException) {
            throw e
          } catch (t: Throwable) {
            logError("Recomposition", "recomposition tracker failed while releasing recomposer", t)
          }
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
    baselinePending.block { it.clear() }
    passRecords.block { it.clear() }
    pendingInvalidations.block { it.clear() }
    captureJobs.block { it.clear() }
    graftPoints = emptySet()
    windowIndices.clear()
    windowNodes.clear()
    windowCounter = 0
    registry.clear()
    roots = emptyList()
    captureCount = 0
    captureNanosTotal = 0L
    captureNanosMax = 0L
    groupCounts.clear()
    captureStats.value = CaptureStats(0, 0, 0f, 0f)
    version.value++
  }

  private var reportingExistingCompositions = false

  private fun track(info: RecomposerInfo) {
    if (info in registrationHandles) return
    reportingExistingCompositions = true
    val handle = try {
      info.observe(registrationObserver) ?: return
    } finally {
      reportingExistingCompositions = false
    }
    registrationHandles[info] = handle
  }

  private fun untrack(key: Any) {
    registrationHandles.remove(key)?.dispose()
  }

  internal fun snapshotRoots(): List<ComposableNode> = roots

  fun resetCounts() {
    commands.trySend(Command.ResetCounts)
  }

  // cost of reading slot tables on the app's composition thread, so the observer effect stays visible
  class CaptureStats(
    val captures: Int,
    val groups: Int,
    val averageMillis: Float,
    val maxMillis: Float,
  )

  val captureStats: StateFlow<CaptureStats>
    field = MutableStateFlow(CaptureStats(0, 0, 0f, 0f))

  private var captureCount = 0
  private var captureNanosTotal = 0L
  private var captureNanosMax = 0L
  private val groupCounts = HashMap<ObservableComposition, Int>()

  fun dump(): String = buildString {
    fun ComposableNode.dumpTo(indent: String) {
      append(indent).append(name)
      append(" comps=").append(compositionCount)
      append(" skips=").append(skipCount)
      bounds?.let { append(" @").append(it.left.toInt()).append(',').append(it.top.toInt()).append(' ').append(it.width.toInt()).append('x').append(it.height.toInt()) }
      if (parameters.isNotEmpty()) {
        append(" params=").append(parameters.joinToString(", ") { "${it.name}=${it.value.displayValue.take(40)}" })
      }
      append('\n')
      children.forEach { it.dumpTo("$indent  ") }
    }
    roots.forEach { it.dumpTo("") }
  }

  private val baselinePending = ConcurrentMutableMap<ObservableComposition, Unit>()

  private val registrationObserver = object : CompositionRegistrationObserver {
    override fun onCompositionRegistered(composition: ObservableComposition) {
      if (composition.isLivewireOwned()) return

      val handle = composition.setObserver(compositionObserverFor(composition))
      compositionObserverHandles.block { it[composition] = handle }
      if (reportingExistingCompositions) {
        commands.trySend(Command.Capture(composition, ScopePass.Empty))
        forceRestartLambdas(composition)
      } else {
        // registration happens on the composing thread before the first pass, so this is safe to call directly
        (composition as? CompositionImpl)?.composer?.collectParameterInformation()
      }
    }

    override fun onCompositionUnregistered(composition: ObservableComposition) {
      compositionObserverHandles.remove(composition)?.dispose()
      passRecords.remove(composition)
      commands.trySend(Command.Unregister(composition))
    }
  }

  private fun compositionObserverFor(composition: ObservableComposition) = object : CompositionObserver {
    private fun record(): PassRecord = passRecords.block { it.getOrPut(composition) { PassRecord() } }

    // the composer enters a scope before deciding to skip it, and also enters (without running) scopes whose derived state invalidation
    // turned out to be a noop. exit flag covers the first case, isInvalidFor covers the second.
    override fun onScopeEnter(scope: RecomposeScope) {
      val pending = pendingInvalidations.remove(scope) ?: return
      val willExecute = pending === UnconditionalInvalidation || try {
        (scope as? RecomposeScopeImpl)?.isInvalidFor(pending) ?: true
      } catch (_: Throwable) {
        true
      }
      if (!willExecute) record().reread.add(scope)
    }

    // a pausable composition (lazy prefetch) exits a scope it paused before running it
    override fun onScopeExit(scope: RecomposeScope) {
      val record = record()
      val impl = scope as? RecomposeScopeImpl
      when {
        impl?.paused == true -> record.paused.add(scope)
        impl?.skipped == true -> record.skipped.add(scope)
        record.reread.remove(scope) -> Unit
        else -> record.executed.add(scope)
      }
    }

    override fun onEndComposition(composition: ObservableComposition) {
      val record = passRecords.remove(composition)
      val baseline = baselinePending.remove(composition) != null
      commands.trySend(Command.Capture(composition, record?.toPass(baseline) ?: ScopePass.Empty))
    }

    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
      pendingInvalidations.block { pending ->
        val current = pending[scope]
        when {
          value == null -> pending[scope] = UnconditionalInvalidation
          current === UnconditionalInvalidation -> Unit
          current is MutableScatterSet<*> -> @Suppress("UNCHECKED_CAST") (current as MutableScatterSet<Any>).add(value)
          else -> pending[scope] = MutableScatterSet<Any>().also { it.add(value) }
        }
      }
      commands.trySend(Command.Invalidate(scope, value))
    }

    override fun onScopeDisposed(scope: RecomposeScope) {
      pendingInvalidations.remove(scope)
      commands.trySend(Command.DisposeScope(scope))
    }

    override fun onBeginComposition(composition: ObservableComposition) = Unit
    override fun onReadInScope(scope: RecomposeScope, value: Any) = Unit
  }

  // parameter values are only reachable through a scope's restart lambda, which the composer only keeps for scopes it considers used
  private fun forceRestartLambdas(composition: ObservableComposition) {
    val impl = composition as? CompositionImpl ?: return
    val forced = try {
      impl.composer.forceRecomposeScopes()
    } catch (_: Throwable) {
      false
    }
    if (forced) {
      baselinePending[composition] = Unit
      impl.parent.invalidate(impl)
    }
  }

  private fun process(command: Command) {
    when (command) {
      is Command.Capture -> scheduleCapture(command.composition, command.pass)
      is Command.Rebuild -> rebuild(command.composition, command.snapshot, command.pass)
      is Command.Invalidate -> registry.nodeForScope(command.scope)?.recordInvalidation(command.value)
      is Command.DisposeScope -> registry.unbindScope(command.scope)
      is Command.ResetCounts -> {
        registry.forEachNode { it.resetCounts() }
        requestPublish()
      }
      is Command.Unregister -> {
        captureJobs.remove(command.composition)?.forEach { it.cancel() }
        compositionRoots.remove(command.composition)
        groupCounts.remove(command.composition)
        requestPublish()
      }
    }
  }

  // this is annoying and might have a better solution, but it works for now. so here's why we're doing this:
  // a composition's slot table is only written during its recomposer's frame dispatch, so reading it via withFrameNanos on the
  // compositions's own clock keeps us thread safe. it also keeps the read happening after `apply`
  private fun scheduleCapture(composition: ObservableComposition, pass: ScopePass) {
    if (!compositionObserverHandles.containsKey(composition)) return // disposed while queued

    val scope = trackingScope
    val frameClock = try {
      (composition as? CompositionImpl)?.parent?.effectCoroutineContext[MonotonicFrameClock]
    } catch (_: Throwable) {
      null
    }

    if (scope == null || frameClock == null) {
      // no clock to use as a trampoline, which _SHOULD_ mean the composition is on the main thread and we can read here
      captureSnapshot(composition)?.let { rebuild(composition, it, pass) }
      return
    }

    val job = scope.launch {
      val snapshot = try {
        frameClock.withFrameNanos { captureSnapshot(composition) }
      } catch (e: CancellationException) {
        throw e
      } catch (t: Throwable) {
        logError("Recomposition", "recomposition tracker failed while capturing a composition", t)
        null
      }
      if (snapshot != null) {
        commands.trySend(Command.Rebuild(composition, snapshot, pass))
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
    pass: ScopePass,
  ) {
    if (!compositionObserverHandles.containsKey(composition)) return
    recordCaptureStats(composition, snapshot)
    compositionRoots[composition] = builder.build(snapshot, pass)
    requestPublish()
  }

  private fun captureSnapshot(composition: ObservableComposition): List<GroupSnapshot>? {
    val compositionImpl = composition as? CompositionImpl ?: return null
    val compositionData = try {
      compositionImpl.slotStorage as CompositionData
    } catch (_: Throwable) {
      return null
    }
    val start = MonotonicClock.elapsedNanos()
    val snapshot = builder.snapshot(compositionData.compositionGroups)
    lastCaptureNanos = MonotonicClock.elapsedNanos() - start
    return snapshot
  }

  // written inside the frame callback, read back by rebuild on the tracker thread
  @Volatile
  private var lastCaptureNanos = 0L

  private fun recordCaptureStats(composition: ObservableComposition, snapshot: List<GroupSnapshot>) {
    val elapsed = lastCaptureNanos
    captureCount++
    captureNanosTotal += elapsed
    if (elapsed > captureNanosMax) captureNanosMax = elapsed
    groupCounts[composition] = snapshot.sumOf { it.groupCount() }
    captureStats.value = CaptureStats(
      captures = captureCount,
      groups = groupCounts.values.sum(),
      averageMillis = captureNanosTotal / captureCount / 1_000_000f,
      maxMillis = captureNanosMax / 1_000_000f,
    )
  }

  private fun GroupSnapshot.groupCount(): Int = 1 + children.sumOf { it.groupCount() }

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

internal fun ObservableComposition.isLivewireOwned(): Boolean = try {
  (this as? CompositionImpl)?.parent?.effectCoroutineContext?.get(LivewireComposition) != null
} catch (_: Throwable) {
  false
}

private class PassRecord {
  val executed = HashSet<RecomposeScope>()
  val skipped = HashSet<RecomposeScope>()
  val paused = HashSet<RecomposeScope>()
  val reread = HashSet<RecomposeScope>()

  fun toPass(baseline: Boolean) = ScopePass(executed, skipped, paused, baseline)
}

private val UnconditionalInvalidation = Any()

private sealed interface Command {
  class Capture(
    val composition: ObservableComposition,
    val pass: ScopePass,
  ) : Command
  class Rebuild(
    val composition: ObservableComposition,
    val snapshot: List<GroupSnapshot>,
    val pass: ScopePass,
  ) : Command
  class Invalidate(val scope: RecomposeScope, val value: Any?) : Command
  object ResetCounts : Command
  class DisposeScope(val scope: RecomposeScope) : Command
  class Unregister(val composition: ObservableComposition) : Command
}

private const val PublishThrottleMs = 100L
