package com.r0adkll.livewire.client

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.discovery.DiscoveryPacket
import com.r0adkll.livewire.logDebug
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

actual class DiscoveryBroadcaster {
  actual fun start(
    scope: CoroutineScope,
    instanceId: String?,
    appName: String,
  ) {
    checkNotNull(instanceId) { "attempted to start discovery broadcaster without an instanceId to broadcast" }

    val packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = appName,
      processId = ProcessHandle.current().pid(),
    )
    val json = Json.encodeToString(packet)
    val target = InetSocketAddress("127.0.0.1", LivewireConstants.DiscoveryPort)

    scope.launch {
      logDebug("DiscoveryBroadcaster", "started broadcasting on port ${LivewireConstants.DiscoveryPort}")
      val selectorManager = SelectorManager(Dispatchers.IO)
      val socket = aSocket(selectorManager).udp().bind()

      try {
        while (isActive) {
          try {
            val datagram = Datagram(
              packet = buildPacket { writeText(json) },
              address = target,
            )
            socket.send(datagram)
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            logDebug("DiscoveryBroadcaster", "send failed: ${e.message}")
          }

          delay(2000)
        }
      } finally {
        socket.close()
        selectorManager.close()
      }
    }
  }
}
