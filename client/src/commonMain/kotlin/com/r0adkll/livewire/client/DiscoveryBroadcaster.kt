package com.r0adkll.livewire.client

import kotlinx.coroutines.CoroutineScope

expect class DiscoveryBroadcaster() {
  fun start(
    scope: CoroutineScope,
    instanceId: String?,
    appName: String,
  )
}
