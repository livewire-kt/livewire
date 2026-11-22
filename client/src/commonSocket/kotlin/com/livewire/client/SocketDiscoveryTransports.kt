package com.livewire.client

import com.livewire.LivewireIoDispatcher
import com.livewire.LivewireConstants
import com.livewire.logDebug
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.writeByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal actual fun startUdpBroadcast(scope: CoroutineScope, bytes: ByteArray) {
  val target = InetSocketAddress("127.0.0.1", LivewireConstants.UdpDiscoveryPort)

  scope.launch(LivewireIoDispatcher) {
    logDebug("started UDP broadcasting on port ${LivewireConstants.UdpDiscoveryPort}")
    val selectorManager = SelectorManager(LivewireIoDispatcher)
    val socket = aSocket(selectorManager).udp().bind()

    try {
      while (isActive) {
        try {
          socket.send(
            Datagram(
              packet = buildPacket { write(bytes) },
              address = target,
            ),
          )
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logDebug("UDP send failed: ${e.message}")
        }

        delay(2000)
      }
    } finally {
      socket.close()
      selectorManager.close()
    }
  }
}

internal actual fun startTcpDiscoveryServer(scope: CoroutineScope, bytes: ByteArray) {
  scope.launch(LivewireIoDispatcher) {
    val selectorManager = SelectorManager(LivewireIoDispatcher)
    var boundSocket: ServerSocket? = null

    for (port in LivewireConstants.TcpDiscoveryPorts) {
      try {
        boundSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", port)
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

    try {
      while (isActive) {
        try {
          val clientSocket = boundSocket.accept()
          launch {
            try {
              val channel = clientSocket.openWriteChannel(autoFlush = true)
              channel.writeByteArray(bytes)
              channel.flushAndClose()
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
    }
  }
}

private fun logDebug(message: String) {
  logDebug("DiscoveryBroadcaster", message)
}
