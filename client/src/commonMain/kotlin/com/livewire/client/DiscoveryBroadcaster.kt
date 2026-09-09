package com.livewire.client

import com.livewire.LivewireIoDispatcher
import com.livewire.LivewireConstants
import com.livewire.discovery.DiscoveryPacket
import com.livewire.logDebug
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

expect fun createDiscoveryConfig(instanceId: String): DiscoveryConfig

/**
 * Raw-socket discovery transports. Implemented in the `socketMain` source set for platforms
 * with socket APIs; web has no sockets, so its actuals only log — browsers use
 * [DiscoveryConfig.Transport.Announce] instead.
 */
internal expect fun startUdpBroadcast(scope: CoroutineScope, bytes: ByteArray)

internal expect fun startTcpDiscoveryServer(scope: CoroutineScope, bytes: ByteArray)

class DiscoveryBroadcaster {

  fun start(
    scope: CoroutineScope,
    instanceId: String,
  ) {
    val config = createDiscoveryConfig(instanceId)
    val bytes = DiscoveryPacket.encode(config.packet)

    when (config.transport) {
      Udp -> startUdpBroadcast(scope, bytes)
      Tcp -> startTcpDiscoveryServer(scope, bytes)
      Announce -> startAnnounceLoop(scope, bytes)
    }
  }

  /**
   * Announces over a WebSocket to the host's discovery listener. Used on platforms that can't
   * open raw sockets (browsers) — the host treats each packet like a UDP announcement and prunes
   * the app the moment the socket closes.
   */
  private fun startAnnounceLoop(scope: CoroutineScope, bytes: ByteArray) {
    scope.launch(LivewireIoDispatcher) {
      val client = HttpClient(createPlatformEngine()) {
        install(WebSockets)
      }

      logDebug("started WS announce loop to port ${LivewireConstants.UdpDiscoveryPort}")
      while (isActive) {
        try {
          client.webSocket(
            host = "127.0.0.1",
            port = LivewireConstants.UdpDiscoveryPort,
            path = LivewireConstants.AnnouncePath,
          ) {
            while (isActive) {
              send(Frame.Binary(true, bytes))
              delay(2000)
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logDebug("announce failed: ${e.message}")
        }

        delay(2000)
      }
    }
  }

  private fun logDebug(message: String) {
    logDebug("DiscoveryBroadcaster", message)
  }
}

data class DiscoveryConfig(
  val packet: DiscoveryPacket,
  val transport: Transport,
) {
  enum class Transport { Udp, Tcp, Announce }
}

const val UnknownConfigField = "Unknown"
const val MaxAppIconSizePx = 64
