package com.r0adkll.livewire.runtime.iosbridge

import com.r0adkll.livewire.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.Channels
import java.nio.channels.SocketChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class IosForwarder(
  private val deviceId: Int,
  private val forwardPort: Int,
  private val bridgePort: Int,
  private val socketPath: Path = Paths.get(UsbmuxdPath),
) : AutoCloseable {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val running = AtomicBoolean(false)

  private val activeCloser = AtomicReference<(() -> Unit)?>(null)

  fun start() {
    if (!running.compareAndSet(false, true)) return
    scope.launch { runLoop() }
  }

  override fun close() {
    if (running.compareAndSet(true, false)) {
      activeCloser.getAndSet(null)?.invoke()
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

  private suspend fun runConnected(stream: SocketChannel) {
    val local = runCatching {
      SocketChannel.open(InetSocketAddress(InetAddress.getByName("127.0.0.1"), forwardPort))
    }.getOrNull()

    if (local == null) {
      logDebug("failed to connect to 127.0.0.1:$forwardPort")
      return
    }

    logDebug("connected to 127.0.0.1:$forwardPort")

    activeCloser.set {
      local.closeCatching()
      stream.closeCatching()
    }

    val result = runCatching {
      coroutineScope {
        val toLocal = launch { copyStream(Channels.newInputStream(stream), Channels.newOutputStream(local)) }
        val toStream = launch { copyStream(Channels.newInputStream(local), Channels.newOutputStream(stream)) }

        toLocal.invokeOnCompletion {
          local.closeCatching()
          stream.closeCatching()
        }
        toStream.invokeOnCompletion {
          local.closeCatching()
          stream.closeCatching()
        }
      }
    }

    activeCloser.set(null)

    local.closeCatching()
    stream.closeCatching()

    if (result.isFailure) {
      logDebug("forwarding ended (will reconnect)")
    }
  }

  private fun copyStream(input: InputStream, output: OutputStream) {
    val buffer = ByteArray(16 * 1024)
    try {
      while (true) {
        val read = input.read(buffer)
        if (read < 0) return
        output.write(buffer, 0, read)
        output.flush()
      }
    } catch (_: Throwable) { }
  }

  private fun SocketChannel.closeCatching() = runCatching { this.close() }

  private fun logDebug(message: String) {
    logDebug("ios-forwarder", message)
  }
}
