package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.flow.StateFlow

sealed class InvalidationReason(
  val label: String,
  val value: String?,
  val timestamp: Long,
) {
  class Unknown : InvalidationReason("reason unknown", null, MonotonicClock.elapsedMillis())

  class Direct : InvalidationReason("direct invalidation", null, MonotonicClock.elapsedMillis())

  class Reason(reason: Any) : InvalidationReason(
    label = when (reason) {
      is MutableState<*> -> "Mutable${reason::class.simpleName!!.substringAfter("Mutable")}"
      is SnapshotStateList<*> -> "SnapshotStateList"
      is SnapshotStateMap<*, *> -> "SnapshotStateMap"
      is StateFlow<*> -> "StateFlow"
      else -> reason::class.simpleName ?: "Unknown"
    },
    value = when (val value = if (reason is State<*>) reason.value else reason) {
      null -> "null"
      is String -> "\"$value\""
      is Collection<*> -> "${value::class.simpleName}(size=${value.size})"
      is Map<*, *> -> "${value::class.simpleName}(size=${value.size})"
      else -> value.toString()
    },
    timestamp = MonotonicClock.elapsedMillis(),
  )
}
