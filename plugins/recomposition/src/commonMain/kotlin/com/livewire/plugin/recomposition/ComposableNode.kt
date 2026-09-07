package com.livewire.plugin.recomposition

import kotlin.concurrent.Volatile

internal class ComposableNode(
  val key: Any,
  val name: String,
) {
  private val timestampRing = LongArray(MaxRateSamples)
  private var timestampHead = 0
  @Volatile
  private var timestampCount = 0

  private var enterCount: Int = 0

  var compositionCount: Int = 0
    private set

  var recompositionCount: Int = 0
    private set

  @Volatile
  var lastRecompositionMillis: Long = 0L
    private set

  private var seen = false

  val skipCount: Int
    get() = enterCount - compositionCount

  private val invalidationReasons = BoundedQueue<InvalidationReason>(maxSize = RecentReasonsCount)

  @Volatile
  private var invalidatedSinceLastComposition = false

  var parameters: List<ParameterInfo> = emptyList()

  @Volatile
  var deactivated: Boolean = false

  @Volatile
  var bounds: NodeBounds? = null

  // copy on write so the ui thread can traverse children while the tracker thread rebuilds it
  @Volatile
  var children: List<ComposableNode> = emptyList()
    private set

  private var ownChildren: List<ComposableNode> = emptyList()

  var graftedChildren: List<ComposableNode> = emptyList()
    private set

  fun setChildren(newChildren: List<ComposableNode>) {
    ownChildren = newChildren.toList()
    children = ownChildren + graftedChildren
  }

  fun setGraftedChildren(newChildren: List<ComposableNode>) {
    graftedChildren = newChildren.toList()
    children = ownChildren + graftedChildren
  }

  fun recentInvalidationReasons(): List<InvalidationReason> = invalidationReasons.toList()

  fun recompositionRate(): Float {
    val now = MonotonicClock.elapsedMillis()
    val count = timestampCount
    var recent = 0
    for (i in 0 until count) {
      if (now - timestampRing[i] <= RecompositionRateWindow) recent++
    }
    return recent / (RecompositionRateWindow / 1000f)
  }

  fun recordEnter() {
    enterCount++
  }

  // a node observed in a capture without executing must already have been composed before tracking began
  fun markExisting() {
    seen = true
  }

  fun recordComposition(parentExecuted: Boolean = false, changedArguments: List<String> = emptyList()) {
    val now = MonotonicClock.elapsedMillis()

    if (seen) {
      recompositionCount++
      lastRecompositionMillis = now
      if (!invalidatedSinceLastComposition) {
        invalidationReasons.add(
          when {
            changedArguments.isNotEmpty() -> InvalidationReason.ArgumentsChanged(changedArguments)
            parentExecuted -> InvalidationReason.Parent()
            else -> InvalidationReason.Unknown()
          },
        )
      }
    }
    seen = true
    invalidatedSinceLastComposition = false
    compositionCount++

    recordTimestamp(now)
  }

  private fun recordTimestamp(now: Long) {
    timestampRing[timestampHead] = now
    timestampHead = (timestampHead + 1) % MaxRateSamples
    if (timestampCount < MaxRateSamples) timestampCount += 1
  }

  // fresh means the node is about to hold new content, so its next execution is an initial composition
  fun resetCounts(fresh: Boolean = false) {
    enterCount = 0
    compositionCount = 0
    recompositionCount = 0
    lastRecompositionMillis = 0L
    timestampHead = 0
    timestampCount = 0
    invalidationReasons.clear()
    invalidatedSinceLastComposition = false
    seen = !fresh
  }

  fun recordInvalidation(value: Any?) {
    invalidatedSinceLastComposition = true
    invalidationReasons.add(if (value != null) InvalidationReason.Reason(value) else InvalidationReason.Direct())
  }
}

private const val RecompositionRateWindow = 5000L
private const val RecentReasonsCount = 3

// caps the rate sampling buffer. 300 samples over the 5s window saturates @ 60/s, past the point where the indicator
// would just mean "constantly recomposing" anyway
private const val MaxRateSamples = 300
