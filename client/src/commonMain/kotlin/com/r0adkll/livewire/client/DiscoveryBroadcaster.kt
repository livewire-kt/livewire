package com.r0adkll.livewire.client

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.discovery.DiscoveryPacket
import com.r0adkll.livewire.logDebug
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeText
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

expect fun createDiscoveryConfig(instanceId: String): DiscoveryConfig

class DiscoveryBroadcaster {
  private var tcpServer: ServerSocket? = null

  fun start(
    scope: CoroutineScope,
    instanceId: String,
  ) {
    val config = createDiscoveryConfig(instanceId)
    val json = Json.encodeToString(config.packet)

    when (config.transport) {
      Udp -> startUdpBroadcast(scope, json)
      Tcp -> startTcpServer(scope, json)
    }
  }

  private fun startUdpBroadcast(scope: CoroutineScope, json: String) {
    val target = InetSocketAddress("127.0.0.1", LivewireConstants.UdpDiscoveryPort)

    scope.launch(Dispatchers.IO) {
      logDebug("DiscoveryBroadcaster", "started UDP broadcasting on port ${LivewireConstants.UdpDiscoveryPort}")
      val selectorManager = SelectorManager(Dispatchers.IO)
      val socket = aSocket(selectorManager).udp().bind()

      try {
        while (isActive) {
          try {
            socket.send(
              Datagram(
                packet = buildPacket { writeText(json) },
                address = target,
              ),
            )
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            logDebug("DiscoveryBroadcaster", "UDP send failed: ${e.message}")
          }

          delay(2000)
        }
      } finally {
        socket.close()
        selectorManager.close()
      }
    }
  }

  private fun startTcpServer(scope: CoroutineScope, json: String) {
    scope.launch(Dispatchers.IO) {
      val selectorManager = SelectorManager(Dispatchers.IO)
      var boundSocket: ServerSocket? = null

      for (port in LivewireConstants.TcpDiscoveryPorts) {
        try {
          boundSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", port)
          logDebug("tcp discovery server bound on port $port")
          break
        } catch (_: Exception) {
          logDebug("port $port in use, trying next")
        }
      }

      if (boundSocket == null) {
        logDebug("failed to bind TCP discovery on any port in range")
        selectorManager.close()
        return@launch
      }

      tcpServer = boundSocket

      try {
        while (isActive) {
          try {
            val clientSocket = boundSocket.accept()
            launch {
              try {
                clientSocket
                  .openWriteChannel(autoFlush = true)
                  .writeStringUtf8(json + "\n")
              } catch (e: CancellationException) {
                throw e
              } catch (e: Exception) {
                logDebug("tcp send failed: ${e.message}")
              } finally {
                clientSocket.close()
              }
            }
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            logDebug("tcp accept failed: ${e.message}")
          }
        }
      } finally {
        boundSocket.close()
        selectorManager.close()
        tcpServer = null
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
  enum class Transport { Udp, Tcp }
}

const val UnknownConfigField = "Unknown"
