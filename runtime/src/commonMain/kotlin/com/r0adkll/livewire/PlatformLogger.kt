package com.r0adkll.livewire

expect fun logDebug(tag: String, message: String)

expect fun logError(
  tag: String,
  message: String,
  throwable: Throwable? = null,
)
