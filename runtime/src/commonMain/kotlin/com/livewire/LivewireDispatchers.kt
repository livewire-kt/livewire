package com.livewire

import kotlinx.coroutines.CoroutineDispatcher

/**
 * `Dispatchers.IO` where the platform has one; `Dispatchers.Default` on single-threaded
 * targets (browsers) that don't have an IO dispatcher.
 */
val LivewireIoDispatcher: CoroutineDispatcher
  get() = platformIoDispatcher

internal expect val platformIoDispatcher: CoroutineDispatcher
