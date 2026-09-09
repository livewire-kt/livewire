package com.livewire.discovery

import com.livewire.LivewireConstants

enum class DiscoveryPlatform {
  Android,
  IosSimulator,
  IosPhysical,
  Desktop,
  Web,
}

/**
 * NOTE TO FUTURE SELF: encode and decode parse this by fixed position. NEVER remove, reorder, or
 * change the type of the wire fields. only append, and any such change must bump
 * [LivewireConstants.ProtocolVersion]. older hosts read by fixed offsets and will otherwise
 * misinterpret the packet.
 */
data class DiscoveryPacket(
  val instanceId: String,
  val appName: String,
  val packageName: String,
  val processId: Long,
  val platform: DiscoveryPlatform,
  val deviceName: String,
  val osVersion: String,
  val appIcon: ByteArray? = null,
  val protocolVersion: Int = LivewireConstants.ProtocolVersion,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as DiscoveryPacket

    if (processId != other.processId) return false
    if (protocolVersion != other.protocolVersion) return false
    if (instanceId != other.instanceId) return false
    if (appName != other.appName) return false
    if (packageName != other.packageName) return false
    if (platform != other.platform) return false
    if (deviceName != other.deviceName) return false
    if (osVersion != other.osVersion) return false
    if (!appIcon.contentEquals(other.appIcon)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = processId.hashCode()
    result = 31 * result + protocolVersion
    result = 31 * result + instanceId.hashCode()
    result = 31 * result + appName.hashCode()
    result = 31 * result + packageName.hashCode()
    result = 31 * result + platform.hashCode()
    result = 31 * result + deviceName.hashCode()
    result = 31 * result + osVersion.hashCode()
    result = 31 * result + (appIcon?.contentHashCode() ?: 0)
    return result
  }

  companion object {
    fun encode(packet: DiscoveryPacket): ByteArray {
      val instanceId = packet.instanceId.encodeToByteArray()
      val appName = packet.appName.encodeToByteArray()
      val packageName = packet.packageName.encodeToByteArray()
      val deviceName = packet.deviceName.encodeToByteArray()
      val osVersion = packet.osVersion.encodeToByteArray()
      val appIcon = packet.appIcon

      val size = 4 + 1 +
        (4 + instanceId.size) + (4 + appName.size) +
        (4 + packageName.size) + 8 +
        (4 + deviceName.size) + (4 + osVersion.size) +
        (4 + (appIcon?.size ?: 0))

      val out = CursorByteArray(ByteArray(size))
      out.writeInt(packet.protocolVersion)
      out.writeByte(packet.platform.ordinal.toByte())
      out.writeBytes(instanceId)
      out.writeBytes(appName)
      out.writeBytes(packageName)
      out.writeLong(packet.processId)
      out.writeBytes(deviceName)
      out.writeBytes(osVersion)
      out.writeOptionalBytes(appIcon)
      return out.bytes
    }

    fun decode(bytes: ByteArray): DiscoveryPacket? {
      val reader = CursorByteArray(bytes)
      return try {
        val protocolVersion = reader.readInt()
        val platform = DiscoveryPlatform.entries.getOrNull(reader.readByte().toInt() and 0xFF) ?: return null
        DiscoveryPacket(
          instanceId = reader.readString(),
          appName = reader.readString(),
          packageName = reader.readString(),
          processId = reader.readLong(),
          platform = platform,
          deviceName = reader.readString(),
          osVersion = reader.readString(),
          appIcon = reader.readOptionalBytes(),
          protocolVersion = protocolVersion,
        )
      } catch (_: Exception) {
        null
      }
    }
  }
}

private class CursorByteArray(val bytes: ByteArray) {
  var offset: Int = 0

  fun writeInt(value: Int) {
    bytes[offset++] = (value ushr 24).toByte()
    bytes[offset++] = (value ushr 16).toByte()
    bytes[offset++] = (value ushr 8).toByte()
    bytes[offset++] = value.toByte()
  }

  fun readInt(): Int {
    val value = ((bytes[offset].toInt() and 0xFF) shl 24) or
      ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
      ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
      (bytes[offset + 3].toInt() and 0xFF)
    offset += 4
    return value
  }

  fun writeLong(value: Long) {
    var shift = 56
    while (shift >= 0) {
      bytes[offset++] = (value ushr shift).toByte()
      shift -= 8
    }
  }

  fun readLong(): Long {
    var value = 0L
    repeat(8) { value = (value shl 8) or (bytes[offset++].toLong() and 0xFF) }
    return value
  }

  fun writeByte(value: Byte) {
    bytes[offset++] = value
  }

  fun readByte(): Byte = bytes[offset++]

  fun writeBytes(value: ByteArray) {
    writeInt(value.size)
    value.copyInto(bytes, offset)
    offset += value.size
  }

  fun readBytes(): ByteArray {
    val length = readInt()
    val result = bytes.copyOfRange(offset, offset + length)
    offset += length
    return result
  }

  fun readString(): String = readBytes().decodeToString()

  fun writeOptionalBytes(value: ByteArray?) {
    if (value == null) writeInt(-1) else writeBytes(value)
  }

  fun readOptionalBytes(): ByteArray? {
    val length = readInt()
    if (length < 0) return null
    val result = bytes.copyOfRange(offset, offset + length)
    offset += length
    return result
  }
}
