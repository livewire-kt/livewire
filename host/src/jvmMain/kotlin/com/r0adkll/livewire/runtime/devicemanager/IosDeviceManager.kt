package com.r0adkll.livewire.runtime.devicemanager

import com.r0adkll.livewire.LivewireConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.concurrent.atomic.AtomicReference

@Serializable
data class IosDeviceInfo(
  val udid: String,
  val name: String,
  @SerialName("product_type")
  val productType: String,
  @SerialName("device_type")
  val deviceType: IosDeviceType,
  @SerialName("os_version")
  val osVersion: String,
) : HostDevice {
  override val id: String = "ios:$udid"
  override val displayName: String = when (deviceType) {
    IosDeviceType.Physical -> "$name (iOS device)"
    IosDeviceType.Simulator -> "$name (iOS simulator)"
  }
}

@Serializable
enum class IosDeviceType {
  @SerialName("physical")
  Physical,

  @SerialName("simulator")
  Simulator,
}

object IosDeviceManager : PlatformDeviceManager {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val json = Json { ignoreUnknownKeys = true }

  private val _devices = MutableStateFlow<List<IosDeviceInfo>>(emptyList())
  override val devices: Flow<List<IosDeviceInfo>> = _devices
  private val pendingActivate = AtomicReference<CompletableDeferred<Boolean>?>(null)
  private val pendingDeactivate = AtomicReference<CompletableDeferred<Boolean>?>(null)

  private val processRef = AtomicReference<Process?>(null)
  private val socketRef = AtomicReference<Socket?>(null)
  private val writerRef = AtomicReference<BufferedWriter?>(null)
  private val startedLock = Mutex()

  override suspend fun ensureStarted() {
    if (processRef.get() != null && socketRef.get() != null) return
    startedLock.withLock {
      if (processRef.get() != null && socketRef.get() != null) return
      startBridge()
    }
  }

  override fun shutdown() {
    writerRef.getAndSet(null)?.runCatching {
      write(json.encodeToString(BridgeRequest.serializer(), BridgeRequest.Deactivate))
      write("\n")
      flush()
    }
    socketRef.getAndSet(null)?.runCatching { close() }
    processRef.getAndSet(null)?.runCatching { destroy() }
    pendingActivate.getAndSet(null)?.complete(false)
    pendingDeactivate.getAndSet(null)?.complete(false)
    _devices.value = emptyList()
  }

  suspend fun activate(udid: String): Boolean {
    ensureStarted()
    val deferred = CompletableDeferred<Boolean>()
    pendingActivate.set(deferred)
    send(BridgeRequest.Activate(udid))
    return deferred.await()
  }

  suspend fun deactivate(): Boolean {
    if (socketRef.get() == null) return true
    val deferred = CompletableDeferred<Boolean>()
    pendingDeactivate.set(deferred)
    send(BridgeRequest.Deactivate)
    return deferred.await()
  }

  private fun startBridge() {
    val projectDir = File(System.getProperty("user.dir")).parentFile
    val bridgeBinary = File(projectDir, "tools/ios-device-bridge/target/release/livewire-ios-bridge")
    val process = ProcessBuilder(
      bridgeBinary.absolutePath,
      "--forward-port=${LivewireConstants.Port}",
      "--multiplex-port=${LivewireConstants.MultiplexPort}",
    )
      .start()

    processRef.set(process)
    scope.launch {
      process.errorStream.bufferedReader().forEachLine { line ->
        System.err.println("Livewire iOS bridge: $line")
      }
    }

    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val line = reader.readLine() ?: return
    val started = json.decodeFromString(BridgeStarted.serializer(), line)

    val socket = Socket("127.0.0.1", started.port)
    socketRef.set(socket)
    val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    writerRef.set(writer)

    sendBlocking(BridgeRequest.Hello(started.token))

    scope.launch {
      val sockReader = BufferedReader(InputStreamReader(socket.getInputStream()))
      while (true) {
        val line = sockReader.readLine() ?: break
        when (val message = json.decodeFromString(BridgeResponse.serializer(), line)) {
          is BridgeResponse.Devices -> {
            _devices.value = message.devices
          }
          is BridgeResponse.Activated -> {
            pendingActivate.getAndSet(null)?.complete(message.ok)
          }
          is BridgeResponse.Deactivated -> {
            pendingDeactivate.getAndSet(null)?.complete(message.ok)
          }
          is BridgeResponse.Error -> {
            pendingActivate.getAndSet(null)?.complete(false)
            pendingDeactivate.getAndSet(null)?.complete(false)
          }
          is BridgeResponse.Hello -> {
            if (!message.ok) {
              pendingActivate.getAndSet(null)?.complete(false)
              pendingDeactivate.getAndSet(null)?.complete(false)
            }
          }
        }
      }
    }
  }

  private suspend fun send(message: BridgeRequest) {
    val writer = writerRef.get() ?: return
    startedLock.withLock {
      writer.write(json.encodeToString(BridgeRequest.serializer(), message))
      writer.write("\n")
      writer.flush()
    }
  }

  private fun sendBlocking(message: BridgeRequest) {
    val writer = writerRef.get() ?: return
    writer.write(json.encodeToString(BridgeRequest.serializer(), message))
    writer.write("\n")
    writer.flush()
  }
}

@Serializable
private data class BridgeStarted(
  val port: Int,
  val token: String,
)

@Serializable
private sealed class BridgeRequest {
  @Serializable
  @SerialName("hello")
  data class Hello(val token: String) : BridgeRequest()

  @Serializable
  @SerialName("activate")
  data class Activate(val udid: String) : BridgeRequest()

  @Serializable
  @SerialName("deactivate")
  data object Deactivate : BridgeRequest()
}

@Serializable
private sealed class BridgeResponse {
  @Serializable
  @SerialName("hello")
  data class Hello(val ok: Boolean, val version: Int) : BridgeResponse()

  @Serializable
  @SerialName("devices")
  data class Devices(val devices: List<IosDeviceInfo>) : BridgeResponse()

  @Serializable
  @SerialName("activated")
  data class Activated(val ok: Boolean, val udid: String? = null) : BridgeResponse()

  @Serializable
  @SerialName("deactivated")
  data class Deactivated(val ok: Boolean) : BridgeResponse()

  @Serializable
  @SerialName("status")
  data class Error(val message: String) : BridgeResponse()
}
