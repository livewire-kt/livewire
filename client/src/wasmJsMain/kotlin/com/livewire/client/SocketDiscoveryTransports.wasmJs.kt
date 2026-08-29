package com.livewire.client

import com.livewire.logError
import kotlinx.coroutines.CoroutineScope

internal actual fun startUdpBroadcast(scope: CoroutineScope, bytes: ByteArray) {
  logError("DiscoveryBroadcaster", "UDP discovery is not supported in browsers; use Transport.Announce", null)
}

internal actual fun startTcpDiscoveryServer(scope: CoroutineScope, bytes: ByteArray) {
  logError("DiscoveryBroadcaster", "TCP discovery is not supported in browsers; use Transport.Announce", null)
}
