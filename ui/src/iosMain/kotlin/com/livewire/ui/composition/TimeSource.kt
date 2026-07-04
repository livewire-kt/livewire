package com.livewire.ui.composition

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.posix.CLOCK_MONOTONIC_RAW
import platform.posix.clock_gettime_nsec_np

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalForeignApi::class)
internal actual inline fun nanoTime(): Long = clock_gettime_nsec_np(CLOCK_MONOTONIC_RAW.toUInt()).convert<Long>()
