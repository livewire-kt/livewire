package com.r0adkll.livewire.ui.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.molecule.RecompositionMode
import app.cash.molecule.SnapshotNotifier
import app.cash.molecule.moleculeFlow
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.LayoutNodePatch
import com.r0adkll.livewire.ui.layout.RootNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

sealed class LivewireOutput {
  data class FullTree(val root: LayoutNode) : LivewireOutput()
  data class Patches(val patches: List<LayoutNodePatch>) : LivewireOutput()
}

fun livewireFlow(
  serializationStrategy: LayoutNodeSerializationStrategy,
  body: @Composable () -> Unit,
): Flow<LivewireOutput> = flow {
  coroutineScope {
    val clock = GatedFrameClock(this, EmptyCoroutineContext)
    val outputBuffer = Channel<LivewireOutput>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    launch(clock, start = UNDISPATCHED) {
      launchLivewire(
        mode = RecompositionMode.ContextClock,
        strategy = serializationStrategy,
        emitter = {
          clock.isRunning = false
          outputBuffer.trySend(it).getOrThrow()
        },
        body = body,
      )
    }

    while (true) {
      val result = outputBuffer.tryReceive()
      // Per `ReceiveChannel.tryReceive` documentation: isFailure means channel is empty.
      val value = if (result.isFailure) {
        clock.isRunning = true
        outputBuffer.receive()
      } else {
        result.getOrThrow()
      }
      emit(value)
    }
  }
}

fun CoroutineScope.launchLivewire(
  mode: RecompositionMode,
  context: CoroutineContext = EmptyCoroutineContext,
  body: @Composable () -> Unit,
): StateFlow<LayoutNode> {
  var flow: MutableStateFlow<LayoutNode>? = null

  launchLivewire(
    context = context,
    mode = mode,
    strategy = null,
    emitter = { value ->
      if (value is LivewireOutput.FullTree) {
        val outputFlow = flow
        if (outputFlow != null) {
          outputFlow.value = value.root
        } else {
          flow = MutableStateFlow(value.root)
        }
      }
    },
    body = body,
  )

  return flow!!
}

/**
 * Launch a coroutine into this [CoroutineScope] which will continually recompose `body`
 * in the optional [context] to invoke [emitter] with each returned [T] value.
 *
 * [launchLivewire]'s [emitter] is always free-running and will not respect backpressure.
 * Use [moleculeFlow] to create a backpressure-capable flow.
 *
 * The coroutine context is inherited from the [CoroutineScope].
 * Additional context elements can be specified with [context] argument.
 */
internal fun CoroutineScope.launchLivewire(
  mode: RecompositionMode,
  emitter: (output: LivewireOutput) -> Unit,
  strategy: LayoutNodeSerializationStrategy?,
  context: CoroutineContext = EmptyCoroutineContext,
  snapshotNotifier: SnapshotNotifier = SnapshotNotifier.WhileActive,
  body: @Composable () -> Unit,
) {
  val clockContext = when (mode) {
    RecompositionMode.ContextClock -> EmptyCoroutineContext
    RecompositionMode.Immediate -> GatedFrameClock(this, context)
  }
  val finalContext = coroutineContext + context + clockContext

  val rootNode = RootNode()
  val livewireApplier = LivewireApplier(rootNode, onOutput = emitter, serializationStrategy = strategy)

  val recomposer = Recomposer(finalContext)
  val composition = Composition(livewireApplier, recomposer)

  var snapshotHandle: ObserverHandle? = null
  launch(finalContext, start = UNDISPATCHED) {
    try {
      recomposer.runRecomposeAndApplyChanges()
    } finally {
      composition.dispose()
      snapshotHandle?.dispose()
    }
  }

  when (snapshotNotifier) {
    SnapshotNotifier.External -> {}
    SnapshotNotifier.WhileActive -> {
      var applyScheduled = false
      snapshotHandle = Snapshot.registerGlobalWriteObserver {
        if (!applyScheduled) {
          applyScheduled = true
          launch(finalContext) {
            applyScheduled = false
            Snapshot.sendApplyNotifications()
          }
        }
      }
    }
  }

  composition.setContent(body)
}
