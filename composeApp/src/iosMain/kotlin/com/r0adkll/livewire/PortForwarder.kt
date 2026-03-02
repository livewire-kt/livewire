@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.r0adkll.livewire

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
import platform.posix.EINPROGRESS
import platform.posix.F_GETFL
import platform.posix.F_SETFL
import platform.posix.INADDR_LOOPBACK
import platform.posix.O_NONBLOCK
import platform.posix.POLLERR
import platform.posix.POLLHUP
import platform.posix.POLLOUT
import platform.posix.SOCK_STREAM
import platform.posix.SOL_SOCKET
import platform.posix.SO_ERROR
import platform.posix.SO_NOSIGPIPE
import platform.posix.SO_REUSEADDR
import platform.posix.accept
import platform.posix.bind
import platform.posix.bzero
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.getsockopt
import platform.posix.listen
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.read
import platform.posix.setsockopt
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.strerror
import platform.posix.write

class PortForwarder(
  private val forwardPort: UInt,
  private val bridgePort: UInt,
) {
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private var serverSocket: Int? = null
  private var peerSocket: Int? = null
  private var acceptJob: Job? = null
  private var readJob: Job? = null

  private var activeSocket: Int? = null

  fun start() {
    if (serverSocket != null) return

    val socket = openServerSocket(bridgePort.toUShort(), INADDR_LOOPBACK)
    if (socket == null) {
      logDebug("listen failed")
      return
    }

    serverSocket = socket

    acceptJob?.cancel()
    acceptJob = scope.launch {
      while (scope.isActive && socket == serverSocket) {
        val clientSocket = acceptClient(socket) ?: continue

        peerSocket?.let { close(it) }
        peerSocket = clientSocket

        closeActiveSocket()
        logDebug("connection accepted")
        startForwarding(clientSocket)
      }
    }

    logDebug("listening on 127.0.0.1:$bridgePort")
  }

  fun stop() {
    acceptJob?.cancel()
    readJob?.cancel()

    peerSocket?.let { close(it) }
    peerSocket = null

    serverSocket?.let { close(it) }
    serverSocket = null

    closeActiveSocket()
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
      if (errno == platform.posix.EINTR) return@memScoped null
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

  private fun startForwarding(socket: Int) {
    readJob?.cancel()
    readJob = scope.launch {
      try {
        closeActiveSocket()

        logDebug("opening local socket to 127.0.0.1:$forwardPort")
        val localSocket = connectLocalSocket(forwardPort)
        if (localSocket == null) {
          logDebug("failed to connect to 127.0.0.1:$forwardPort")
          close(socket)
          return@launch
        }

        if (activeSocket != null) {
          logDebug("rejecting local socket since there is already an active socket")
          close(localSocket)
          return@launch
        }

        activeSocket = localSocket
        logDebug("connected to 127.0.0.1:$forwardPort")

        val peerToLocal = launch { pump(socket, localSocket) }
        val localToPeer = launch { pump(localSocket, socket) }

        peerToLocal.join()
        localToPeer.cancel()
      } catch (t: Throwable) {
        logError("forwarding exception", t)
      } finally {
        if (socket == peerSocket) {
          peerSocket = null
        }
        closeActiveSocket()
        close(socket)
        logDebug("channel ended")
      }
    }
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

  private fun closeActiveSocket() {
    activeSocket?.let { close(it) }
    activeSocket = null
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

private fun connectLocalSocket(port: UInt): Int? = memScoped {
  val socket = socket(AF_INET, SOCK_STREAM, 0).takeIf { isValidSocket(it) } ?: return null

  setsockopt(
    socket,
    SOL_SOCKET,
    SO_NOSIGPIPE,
    alloc<IntVar> { value = 1 }.ptr,
    sizeOf<IntVar>().toUInt(),
  )

  val addr = alloc<sockaddr_in> {
    bzero(ptr, sizeOf<sockaddr_in>().toULong())
    sin_family = AF_INET.toUByte()
    sin_port = hostToNetworkShort(port.toUShort())
    sin_addr.s_addr = hostToNetworkInt(0x7F000001u) // 127.0.0.1 in network order
  }

  val flags = fcntl(socket, F_GETFL, 0)
  fcntl(socket, F_SETFL, flags or O_NONBLOCK)

  val result = connect(socket, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().toUInt())
  if (result != 0 && errno != EINPROGRESS) {
    val err = errno
    logDebug("connect failed errno=$err (${strerror(err)})")
    close(socket)
    return null
  }

  if (result != 0) {
    val pollRequest = alloc<pollfd> {
      fd = socket
      events = POLLOUT.toShort()
      revents = 0
    }

    val pollResult = poll(pollRequest.ptr, 1u, 2000)
    if (pollResult <= 0 || (pollRequest.revents.toInt() and (POLLERR or POLLHUP)) != 0) {
      logDebug("connect poll timeout or error (revents=${pollRequest.revents.toInt()})")
      close(socket)
      return null
    }

    val socketError = alloc<IntVar>()
    getsockopt(
      socket,
      SOL_SOCKET,
      SO_ERROR,
      socketError.ptr,
      alloc<UIntVar> { value = sizeOf<IntVar>().toUInt() }.ptr,
    )
    if (socketError.value != 0) {
      val err = socketError.value
      logDebug("connect SO_ERROR=$err (${strerror(err)})")
      close(socket)
      return null
    }
  }

  fcntl(socket, F_SETFL, flags)
  socket
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
