package com.livewire.runtime.iosbridge

import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.NSObject
import com.dd.plist.NSString
import com.dd.plist.PropertyListParser
import java.io.ByteArrayInputStream
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel
import java.nio.file.Path
import kotlin.collections.set

internal class UsbMuxClient private constructor(
  private val socketPath: Path,
  private val channel: SocketChannel,
) {
  private var nextTag: Int = 1

  companion object {
    fun connect(path: Path): UsbMuxClient {
      val address = UnixDomainSocketAddress.of(path)
      val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
      channel.connect(address)
      return UsbMuxClient(path, channel)
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
      val (packetTag, payloadDict) = readUsbMuxPacket()
      if (packetTag == tag) {
        break
      }
      if (packetTag == 0) {
        parseUsbMuxEvent(payloadDict)?.let { initialEvents.add(it) }
      }
    }
    return initialEvents
  }

  fun nextEvent(): UsbMuxEvent? {
    while (true) {
      val (packetTag, payloadDict) = runCatching { readUsbMuxPacket() }.getOrNull() ?: return null
      if (packetTag != 0) continue
      parseUsbMuxEvent(payloadDict)?.let { return it }
    }
  }

  fun connectToDevice(deviceId: Int, port: Int): SocketChannel {
    val payload = NSDictionary()
    payload["DeviceID"] = NSNumber(deviceId)
    payload["PortNumber"] = NSNumber(((port shl 8) and 0xFF00) or (port shr 8)) // Convert to big endian
    val packet = plistPacket("Connect", payload)

    val stream = SocketChannel.open(StandardProtocolFamily.UNIX)
    stream.connect(UnixDomainSocketAddress.of(socketPath))

    val tag = nextTag()
    sendUsbMuxPlist(tag, packet, stream)
    val response = readUsbMuxPlist(tag, stream)

    val dict = response as? NSDictionary
    val code = dict?.objectForKey("Number") as? NSNumber
    if (code != null && code.intValue() != 0) {
      stream.close()
      throw IllegalStateException("usbmux connect failed: ${code.intValue()}")
    }

    return stream
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
      val (packetTag, payload) = readUsbMuxPacket(target)
      if (packetTag != tag) continue
      return payload ?: NSDictionary()
    }
  }

  private fun sendUsbMuxPacket(tag: Int, payload: ByteArray, target: SocketChannel) {
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

  private fun readUsbMuxPacket(): Pair<Int, NSDictionary?> = readUsbMuxPacket(channel)

  private fun readUsbMuxPacket(target: SocketChannel): Pair<Int, NSDictionary?> {
    val sizeBuf = ByteArray(4)
    readFully(target, sizeBuf)
    val size = ByteBuffer.wrap(sizeBuf).order(ByteOrder.LITTLE_ENDIAN).int
    if (size < 16) throw IllegalStateException("short usbmux packet")
    val rest = ByteArray(size - 4)
    readFully(target, rest)
    val tag = ByteBuffer.wrap(rest, 8, 4).order(ByteOrder.LITTLE_ENDIAN).int
    val payload = if (rest.size > 12) rest.copyOfRange(12, rest.size) else ByteArray(0)
    val plist = if (payload.isNotEmpty()) {
      PropertyListParser.parse(ByteArrayInputStream(payload)) as? NSDictionary
    } else {
      null
    }
    return tag to plist
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

  private fun readFully(channel: SocketChannel, buffer: ByteArray) {
    var offset = 0
    while (offset < buffer.size) {
      val read = channel.read(ByteBuffer.wrap(buffer, offset, buffer.size - offset))
      if (read < 0) throw IllegalStateException("unexpected eof")
      offset += read
    }
  }
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
