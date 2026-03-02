package com.r0adkll.livewire.runtime.iosbridge

import com.r0adkll.livewire.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily
import java.nio.channels.Channels
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

internal class IosForwarder(
  private val deviceId: Int,
  private val forwardPort: Int,
  private val bridgePort: Int,
  private val socketPath: Path = Paths.get(UsbmuxdPath),
) {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val running = AtomicBoolean(false)

  fun start() {
    if (!running.compareAndSet(false, true)) return
    scope.launch { runLoop() }
  }

  fun stop() {
    if (running.compareAndSet(true, false)) {
      scope.coroutineContext[Job]?.cancel()
    }
  }

  private suspend fun runLoop() {
    while (running.get()) {
      val client = runCatching { UsbMuxClient.connect(socketPath) }.getOrNull()
      if (client == null) {
        logDebug("failed to connect to usbmuxd at $socketPath")
        delay(500)
        continue
      }

      val stream = runCatching { client.connectToDevice(deviceId, bridgePort) }.getOrNull()
      if (stream == null) {
        logDebug("failed to connect to deviceId=$deviceId bridgePort=$bridgePort")
        client.close()
        delay(500)
        continue
      }

      logDebug("connected deviceId=$deviceId bridgePort=$bridgePort")
      client.close()

      val ok = runCatching { runConnected(stream) }.isSuccess
      if (!ok) {
        logDebug("connection loop ended (will retry)")
      }

      runCatching { stream.close() }
      if (!ok) {
        delay(500)
      }
    }
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  private suspend fun runConnected(stream: SocketChannel) {
    val server = ServerSocketChannel.open(StandardProtocolFamily.INET)
    server.bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), forwardPort))
    logDebug("listening on 127.0.0.1:$forwardPort")

    while (running.get()) {
      val socket = runCatching { server.accept() }.getOrNull() ?: break
      logDebug("accepted local socket, starting stream")
      val result = runCatching {
        coroutineScope {
          val toSocket = launch { copyStream(Channels.newInputStream(stream), Channels.newOutputStream(socket)) }
          val toStream = launch { copyStream(Channels.newInputStream(socket), Channels.newOutputStream(stream)) }

          toSocket.join()
          toStream.cancelAndJoin()
        }
      }

      runCatching { socket.close() }
      runCatching { stream.close() }

      if (result.isFailure) {
        logDebug("forwarding ended (will reconnect)")
      }
      break
    }

    runCatching { server.close() }
  }

  private fun copyStream(input: InputStream, output: OutputStream) {
    val buffer = ByteArray(16 * 1024)
    while (true) {
      val read = input.read(buffer)
      if (read < 0) return
      output.write(buffer, 0, read)
      output.flush()
    }
  }

  private fun logDebug(message: String) {
    logDebug("ios-forwarder", message)
  }
}
