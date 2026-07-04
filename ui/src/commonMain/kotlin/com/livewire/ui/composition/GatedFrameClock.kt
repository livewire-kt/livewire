package com.livewire.ui.composition

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.MonotonicFrameClock
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A [MonotonicFrameClock] that is either running, or not.
 *
 * While running, any request for a frame immediately succeeds. If stopped, requests for a frame wait until
 * the clock is set to run again.
 */
internal class GatedFrameClock(
  scope: CoroutineScope,
  context: CoroutineContext,
  private val frameMutex: Mutex? = null,
) : MonotonicFrameClock {
  private val frameSends = Channel<Unit>(CONFLATED)

  init {
    scope.launch(context) {
      for (send in frameSends) {
        if (!isRunning) continue
        if (frameMutex == null) {
          sendFrame()
        } else {
          frameMutex.withLock { sendFrame() }
        }
      }
    }
  }

  @Volatile
  var isRunning: Boolean = true
    set(value) {
      val started = value && !field
      field = value
      if (started) {
        // run on dispatch loop so mutex works
        frameSends.trySend(Unit)
      }
    }

  @Volatile
  private var lastNanos = 0L
  @Volatile
  private var lastOffset = 0

  private fun sendFrame() {
    val timeNanos = nanoTime()

    // Since we only have millisecond resolution, ensure the nanos form always increases by
    // incrementing a nano offset if we collide with the previous timestamp.
    val offset = if (timeNanos == lastNanos) {
      lastOffset + 1
    } else {
      lastNanos = timeNanos
      0
    }
    lastOffset = offset

    clock.sendFrame(timeNanos + offset)
  }

  private val clock = BroadcastFrameClock {
    if (isRunning) frameSends.trySend(Unit).getOrThrow()
  }

  override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
    return clock.withFrameNanos(onFrame)
  }
}
