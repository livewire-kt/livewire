@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.livewire

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.posix.AF_INET
import platform.posix.INADDR_LOOPBACK
import platform.posix.SOCK_STREAM
import platform.posix.SOL_SOCKET
import platform.posix.SO_NOSIGPIPE
import platform.posix.SO_REUSEADDR
import platform.posix.accept
import platform.posix.bind
import platform.posix.bzero
import platform.posix.close
import platform.posix.listen
import platform.posix.read
import platform.posix.setsockopt
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.write

class PortForwarder(
  private val forwardPort: UInt,
  private val bridgePort: UInt,
) {
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private var bridgeServerSocket: Int? = null
  private var localServerSocket: Int? = null
  private var forwardJob: Job? = null

  fun start() {
    if (bridgeServerSocket != null) return

    val bridge = openServerSocket(bridgePort.toUShort(), INADDR_LOOPBACK)
    if (bridge == null) {
      logDebug("bridge listen failed on $bridgePort")
      return
    }

    val local = openServerSocket(forwardPort.toUShort(), INADDR_LOOPBACK)
    if (local == null) {
      logDebug("local listen failed on $forwardPort")
      close(bridge)
      return
    }

    bridgeServerSocket = bridge
    localServerSocket = local

    logDebug("listening on 127.0.0.1:$bridgePort (USB) and 127.0.0.1:$forwardPort (app)")

    forwardJob = scope.launch {
      while (isActive) {
        val usbSocket = acceptClient(bridge) ?: continue
        logDebug("USB connection accepted")

        val appSocket = acceptClient(local)
        if (appSocket == null) {
          close(usbSocket)
          continue
        }
        logDebug("app connection accepted, bridging")

        try {
          val toApp = launch { pump(usbSocket, appSocket) }
          val toUsb = launch { pump(appSocket, usbSocket) }

          toApp.join()
          toUsb.cancel()
        } catch (t: Throwable) {
          logError("forwarding exception", t)
        } finally {
          close(usbSocket)
          close(appSocket)
          logDebug("bridge ended")
        }
      }
    }
  }

  fun stop() {
    forwardJob?.cancel()
    forwardJob = null

    bridgeServerSocket?.let { close(it) }
    bridgeServerSocket = null

    localServerSocket?.let { close(it) }
    localServerSocket = null

    scope.cancel()
    logDebug("stopped")
  }

  private fun acceptClient(serverSocket: Int): Int? = memScoped {
    val clientSocket = accept(
      serverSocket,
      alloc<sockaddr_in>().ptr.reinterpret(),
      alloc<UIntVar> { value = sizeOf<sockaddr_in>().toUInt() }.ptr,
    )

    if (!isValidSocket(clientSocket)) {
      return@memScoped null
    }

    setsockopt(
      clientSocket,
      SOL_SOCKET,
      SO_NOSIGPIPE,
      alloc<IntVar> { value = 1 }.ptr,
      sizeOf<IntVar>().toUInt(),
    )

    clientSocket
  }

  private fun pump(input: Int, output: Int) {
    val buffer = ByteArray(StreamBufferSize)
    while (true) {
      val readCount = buffer.usePinned { pinned ->
        read(input, pinned.addressOf(0), buffer.size.toULong())
      }
      if (readCount <= 0) return
      if (!writeFully(output, buffer, readCount)) return
    }
  }
}

private fun openServerSocket(port: UShort, address: UInt): Int? {
  val socket = socket(AF_INET, SOCK_STREAM, 0).takeIf { isValidSocket(it) } ?: return null

  val reuseAddrSet = memScoped {
    setsockopt(
      socket,
      SOL_SOCKET,
      SO_REUSEADDR,
      alloc<IntVar> { value = 1 }.ptr,
      sizeOf<IntVar>().toUInt(),
    ) == 0
  }
  if (!reuseAddrSet) {
    close(socket)
    return null
  }

  val socketBound = memScoped {
    val addr = alloc<sockaddr_in> {
      bzero(ptr, sizeOf<sockaddr_in>().toULong())
      sin_family = AF_INET.toUByte()
      sin_port = hostToNetworkShort(port)
      sin_addr.s_addr = hostToNetworkInt(address)
    }

    bind(socket, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().toUInt()) == 0
  }
  if (!socketBound) {
    close(socket)
    return null
  }

  if (listen(socket, 128) != 0) {
    close(socket)
    return null
  }

  return socket
}

private fun hostToNetworkShort(value: UShort): UShort {
  val v = value.toUInt()
  val swapped = ((v and 0xFFu) shl 8) or ((v and 0xFF00u) shr 8)
  return swapped.toUShort()
}

private fun hostToNetworkInt(value: UInt): UInt {
  return ((value and 0x000000FFu) shl 24) or
    ((value and 0x0000FF00u) shl 8) or
    ((value and 0x00FF0000u) shr 8) or
    ((value and 0xFF000000u) shr 24)
}

private fun writeFully(socket: Int, buffer: ByteArray, length: Long): Boolean {
  var offset = 0
  while (offset < length) {
    val writeCount = buffer.usePinned { pinned ->
      write(socket, pinned.addressOf(offset), (length - offset).toULong())
    }
    if (writeCount <= 0) return false
    offset += writeCount.toInt()
  }
  return true
}

private fun isValidSocket(socketFd: Int) = socketFd >= 0

private fun logDebug(message: String) {
  logDebug("livewire-ios-forwarder", message)
}

private fun logError(message: String, throwable: Throwable) {
  logError("livewire-ios-forwarder", message, throwable)
}

private const val StreamBufferSize = 16 * 1024
