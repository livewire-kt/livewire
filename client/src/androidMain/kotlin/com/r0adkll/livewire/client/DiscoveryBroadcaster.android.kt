package com.r0adkll.livewire.client

import kotlinx.coroutines.CoroutineScope

// Unused on android
actual class DiscoveryBroadcaster {
  actual fun start(
    scope: CoroutineScope,
    instanceId: String?,
    appName: String,
  ) = Unit
}
