package com.livewire.runtime.iosbridge

import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSObject
import com.dd.plist.NSString
import com.dd.plist.PropertyListParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.SocketTimeoutException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.collections.set

internal class UsbMuxClient private constructor(
  private val socketPath: Path,
  private val channel: SocketChannel,
) {
  private var nextTag: Int = 1

  companion object {
    fun connect(path: Path, connectTimeoutMs: Long = ConnectTimeoutMs): UsbMuxClient {
      if (!Files.exists(path)) throw NoSuchFileException(path.toString())
      val channel = openChannel(UnixDomainSocketAddress.of(path), connectTimeoutMs)
      return UsbMuxClient(path, channel)
    }

    private fun openChannel(address: UnixDomainSocketAddress, connectTimeoutMs: Long): SocketChannel {
      val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
      try {
        channel.configureBlocking(false)
        channel.connect(address)
        val deadline = System.nanoTime() + connectTimeoutMs * 1_000_000L
        while (!channel.finishConnect()) {
          val remainingMs = (deadline - System.nanoTime()) / 1_000_000
          if (remainingMs <= 0) {
            throw SocketTimeoutException("usbmux connect timed out after ${connectTimeoutMs}ms")
          }
          Thread.sleep(minOf(remainingMs, 25))
        }
        channel.configureBlocking(true)
        return channel
      } catch (t: Throwable) {
        runCatching { channel.close() }
        throw t
      }
    }
  }

  fun close() {
    runCatching { channel.close() }
  }

  fun listen(): List<UsbMuxEvent> {
    val tag = nextTag()
    val payload = plistPacket("Listen", null)
    sendUsbMuxPlist(tag, payload)

    val initialEvents = mutableListOf<UsbMuxEvent>()
    while (true) {
      when (val result = readUsbMuxPacket()) {
        is PacketResult.Packet -> {
          if (result.tag == tag) break
          if (result.tag == 0) {
            parseUsbMuxEvent(result.payload)?.let { initialEvents.add(it) }
          }
        }
        PacketResult.Idle, PacketResult.Failed ->
          throw IllegalStateException("usbmuxd Listen handshake interrupted")
      }
    }
    return initialEvents
  }

  fun nextEvent(): NextEvent {
    val result = try {
      readUsbMuxPacket()
    } catch (_: Exception) {
      return NextEvent.Closed
    }
    return when (result) {
      is PacketResult.Packet -> {
        if (result.tag != 0) return NextEvent.Idle
        parseUsbMuxEvent(result.payload)?.let { return NextEvent.Event(it) } ?: NextEvent.Idle
      }
      PacketResult.Idle -> NextEvent.Idle
      PacketResult.Failed -> NextEvent.Closed
    }
  }

  fun connectToDevice(deviceId: Int, port: Int): SocketChannel {
    val payload = NSDictionary()
    payload["DeviceID"] = NSNumber(deviceId)
    payload["PortNumber"] = NSNumber(((port shl 8) and 0xFF00) or (port shr 8)) // Convert to big endian
    val packet = plistPacket("Connect", payload)

    val stream = openChannel(UnixDomainSocketAddress.of(socketPath), ConnectTimeoutMs)

    return try {
      val tag = nextTag()
      sendUsbMuxPlist(tag, packet, stream)
      val response = readUsbMuxPlist(tag, stream)

      val dict = response as? NSDictionary
      val code = dict?.objectForKey("Number") as? NSNumber
      if (code != null && code.intValue() != 0) {
        throw IllegalStateException("usbmux connect failed: ${code.intValue()}")
      }

      stream
    } catch (t: Throwable) {
      runCatching { stream.close() }
      throw t
    }
  }

  private fun nextTag(): Int = nextTag++

  private fun sendUsbMuxPlist(tag: Int, payload: NSDictionary) {
    sendUsbMuxPlist(tag, payload, channel)
  }

  private fun sendUsbMuxPlist(tag: Int, payload: NSDictionary, target: SocketChannel) {
    val xml = payload.toXMLPropertyList().toByteArray(Charsets.UTF_8)
    sendUsbMuxPacket(tag, xml, target)
  }

  private fun readUsbMuxPlist(tag: Int, target: SocketChannel): NSObject {
    while (true) {
      when (val result = readUsbMuxPacket(target)) {
        is PacketResult.Packet -> if (result.tag == tag) return result.payload ?: NSDictionary()
        PacketResult.Idle, PacketResult.Failed ->
          throw IllegalStateException("usbmux response interrupted")
      }
    }
  }

  private fun sendUsbMuxPacket(tag: Int, payload: ByteArray, target: SocketChannel) {
    target.configureBlocking(true)
    val size = 16 + payload.size
    val header = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
    header.putInt(size)
    header.putInt(1)
    header.putInt(8)
    header.putInt(tag)
    header.flip()
    target.write(header)
    if (payload.isNotEmpty()) {
      target.write(ByteBuffer.wrap(payload))
    }
  }

  private fun readUsbMuxPacket(): PacketResult = readUsbMuxPacket(channel)

  private fun readUsbMuxPacket(target: SocketChannel): PacketResult {
    val sizeBuf = ByteArray(4)
    when (val outcome = readFully(target, sizeBuf, PacketTimeoutMs)) {
      is ReadOutcome.TimedOut ->
        return if (outcome.bytesRead == 0) PacketResult.Idle else PacketResult.Failed
      ReadOutcome.Full -> {}
    }
    val size = ByteBuffer.wrap(sizeBuf).order(ByteOrder.LITTLE_ENDIAN).int
    if (size < 16) return PacketResult.Failed
    val rest = ByteArray(size - 4)
    when (val outcome = readFully(target, rest, PacketTimeoutMs)) {
      is ReadOutcome.TimedOut -> return PacketResult.Failed
      ReadOutcome.Full -> {}
    }
    val tag = ByteBuffer.wrap(rest, 8, 4).order(ByteOrder.LITTLE_ENDIAN).int
    val payload = if (rest.size > 12) rest.copyOfRange(12, rest.size) else ByteArray(0)
    val plist = if (payload.isNotEmpty()) {
      PropertyListParser.parse(ByteArrayInputStream(payload)) as? NSDictionary
    } else {
      null
    }
    return PacketResult.Packet(tag, plist)
  }

  private fun plistPacket(messageType: String, payload: NSDictionary?): NSDictionary {
    val dict = NSDictionary()
    dict["MessageType"] = NSString(messageType)
    dict["ProgName"] = NSString("livewire-ios-bridge")
    dict["ClientVersionString"] = NSString("1")
    if (payload != null) {
      val keys = payload.allKeys()
      for (key in keys) {
        dict[key] = payload.objectForKey(key)
      }
    }
    return dict
  }

  private fun parseUsbMuxEvent(payload: NSDictionary?): UsbMuxEvent? {
    if (payload == null) return null
    val messageType = payload.objectForKey("MessageType").asStringOrNull() ?: return null
    return when (messageType) {
      "Attached" -> {
        val deviceId = payload.objectForKey("DeviceID").asIntOrNull() ?: return null
        val props = payload.objectForKey("Properties") as? NSDictionary
        val properties = props?.toMap() ?: emptyMap()
        val udid = properties["SerialNumber"].asStringOrNull()
        UsbMuxEvent.Attach(deviceId, udid, properties)
      }

      "Detached" -> {
        val deviceId = payload.objectForKey("DeviceID").asIntOrNull() ?: return null
        val udid = payload.objectForKey("SerialNumber").asStringOrNull()
        UsbMuxEvent.Detach(deviceId, udid)
      }

      else -> null
    }
  }

  private fun NSDictionary.toMap(): Map<String, NSObject> = buildMap {
    for (key in allKeys()) {
      val value = objectForKey(key)
      if (value != null) {
        put(key, value)
      }
    }
  }

  private fun NSObject?.asStringOrNull(): String? {
    return when (this) {
      is NSString -> this.content
      is NSNumber -> this.toString()
      else -> this?.toString()
    }
  }

  private fun NSObject?.asIntOrNull(): Int? {
    return when (this) {
      is NSNumber -> this.intValue()
      is NSString -> this.content.toIntOrNull()
      else -> null
    }
  }

  private fun readFully(target: SocketChannel, buffer: ByteArray, timeoutMs: Long): ReadOutcome {
    val selector = Selector.open()
    var offset = 0
    var deadline = System.nanoTime() + timeoutMs * 1_000_000L
    try {
      target.configureBlocking(false)
      target.register(selector, SelectionKey.OP_READ)
      while (offset < buffer.size) {
        val remainingMs = (deadline - System.nanoTime()) / 1_000_000
        if (remainingMs <= 0) return ReadOutcome.TimedOut(offset)
        selector.select(remainingMs)
        selector.selectedKeys().clear()
        val read = target.read(ByteBuffer.wrap(buffer, offset, buffer.size - offset))
        if (read < 0) throw IllegalStateException("unexpected eof from usbmuxd")
        offset += read
      }
      return ReadOutcome.Full
    } finally {
      selector.close()
      runCatching { target.configureBlocking(true) }
    }
  }
}

internal fun SocketChannel.readAllBounded(timeoutMs: Long): ByteArray {
  val out = ByteArrayOutputStream(4096)
  val buffer = ByteArray(4096)
  val selector = Selector.open()
  var deadline = System.nanoTime() + timeoutMs * 1_000_000L
  try {
    configureBlocking(false)
    register(selector, SelectionKey.OP_READ)
    while (true) {
      val remainingMs = (deadline - System.nanoTime()) / 1_000_000
      if (remainingMs <= 0) break
      selector.select(remainingMs)
      selector.selectedKeys().clear()
      val read = read(ByteBuffer.wrap(buffer))
      when {
        read < 0 -> return out.toByteArray() // peer closed, packet complete
        read > 0 -> {
          out.write(buffer, 0, read)
          val grace = System.nanoTime() + ReadGraceMs * 1_000_000L
          if (grace < deadline) deadline = grace
        }
      }
    }
    return out.toByteArray()
  } finally {
    selector.close()
    runCatching { configureBlocking(true) }
  }
}

internal sealed interface NextEvent {
  data class Event(val event: UsbMuxEvent) : NextEvent
  data object Idle : NextEvent
  data object Closed : NextEvent
}

private sealed interface PacketResult {
  data class Packet(val tag: Int, val payload: NSDictionary?) : PacketResult
  data object Idle : PacketResult
  data object Failed : PacketResult
}

private sealed interface ReadOutcome {
  data object Full : ReadOutcome
  data class TimedOut(val bytesRead: Int) : ReadOutcome
}

internal sealed interface UsbMuxEvent {
  data class Attach(
    val deviceId: Int,
    val udid: String?,
    val properties: Map<String, NSObject>,
  ) : UsbMuxEvent

  data class Detach(
    val deviceId: Int,
    val udid: String?,
  ) : UsbMuxEvent
}

internal const val UsbmuxdPath = "/var/run/usbmuxd"

private const val ConnectTimeoutMs = 2000L
private const val PacketTimeoutMs = 3000L
private const val ReadGraceMs = 250L
