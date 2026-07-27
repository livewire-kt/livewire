package com.livewire.runtime

import java.net.BindException

internal fun Throwable.isPortInUse(): Boolean =
  generateSequence(this) { it.cause.takeIf { cause -> cause !== it } }.any { it is BindException }

internal fun Throwable.describe(): String = message ?: this::class.simpleName ?: "unknown error"
