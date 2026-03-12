package com.r0adkll.livewire.client

import kotlinx.coroutines.CoroutineScope

// Unused on ios
actual class DiscoveryBroadcaster {
  actual fun start(
    scope: CoroutineScope,
    instanceId: String?,
    appName: String,
  ) = Unit
}
