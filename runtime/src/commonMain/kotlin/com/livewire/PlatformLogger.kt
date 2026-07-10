package com.livewire

import kotlin.concurrent.Volatile

object LivewireLog {
  @Volatile
  var debugEnabled: Boolean = false
}

fun logDebug(tag: String, message: String) {
  if (LivewireLog.debugEnabled) {
    platformLogDebug(tag, message)
  }
}

fun logError(
  tag: String,
  message: String,
  throwable: Throwable? = null,
) {
  platformLogError(tag, message, throwable)
}

internal expect fun platformLogDebug(tag: String, message: String)

internal expect fun platformLogError(
  tag: String,
  message: String,
  throwable: Throwable?,
)
