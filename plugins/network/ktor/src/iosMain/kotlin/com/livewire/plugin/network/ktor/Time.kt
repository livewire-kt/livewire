package com.livewire.plugin.network.ktor

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMillis(): Long =
  (NSDate().timeIntervalSince1970 * 1000).toLong()
