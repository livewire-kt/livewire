package com.livewire.discovery

import com.livewire.LivewireConstants
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DiscoveryPacketTest {

  private val packet = DiscoveryPacket(
    instanceId = "instance-1",
    appName = "Demo",
    packageName = "com.livewire.demo",
    processId = 4321L,
    platform = DiscoveryPlatform.Android,
    deviceName = "Pixel",
    osVersion = "34",
    appIcon = byteArrayOf(0, 1, 2, 3, 4, 5),
  )

  @Test
  fun roundTripsEveryField() {
    val decoded = DiscoveryPacket.decode(DiscoveryPacket.encode(packet))

    assertNotNull(decoded)
    assertEquals(LivewireConstants.ProtocolVersion, decoded.protocolVersion)
    assertEquals(packet.platform, decoded.platform)
    assertEquals(packet.instanceId, decoded.instanceId)
    assertEquals(packet.appName, decoded.appName)
    assertEquals(packet.packageName, decoded.packageName)
    assertEquals(packet.processId, decoded.processId)
    assertEquals(packet.deviceName, decoded.deviceName)
    assertEquals(packet.osVersion, decoded.osVersion)
    assertContentEquals(packet.appIcon, decoded.appIcon)
  }

  @Test
  fun versionIsReadableFromHeader() {
    val bytes = DiscoveryPacket.encode(packet)
    val headerVersion = ((bytes[0].toInt() and 0xFF) shl 24) or
      ((bytes[1].toInt() and 0xFF) shl 16) or
      ((bytes[2].toInt() and 0xFF) shl 8) or
      (bytes[3].toInt() and 0xFF)

    assertEquals(LivewireConstants.ProtocolVersion, headerVersion)
  }

  @Test
  fun nullAppIconRoundTrips() {
    val decoded = DiscoveryPacket.decode(
      DiscoveryPacket.encode(packet.copy(appIcon = null)),
    )

    assertNull(decoded?.appIcon)
  }

  @Test
  fun incompatibleVersionStillDecodesEveryField() {
    val bytes = DiscoveryPacket.encode(packet)
    bytes[3] = (LivewireConstants.ProtocolVersion + 1).toByte()

    val decoded = DiscoveryPacket.decode(bytes)

    assertNotNull(decoded)
    assertEquals(LivewireConstants.ProtocolVersion + 1, decoded.protocolVersion)
    assertEquals(packet.appName, decoded.appName)
    assertEquals(packet.packageName, decoded.packageName)
    assertContentEquals(packet.appIcon, decoded.appIcon)
  }

  @Test
  fun truncatedPacketDecodesToNull() {
    val bytes = DiscoveryPacket.encode(packet)
    assertNull(DiscoveryPacket.decode(bytes.copyOf(bytes.size - 4)))
  }

  @Test
  fun truncatedHeaderDecodesToNull() {
    assertNull(DiscoveryPacket.decode(byteArrayOf(0, 0, 1)))
  }

  @Test
  fun unknownPlatformTagDecodesToNull() {
    val bytes = DiscoveryPacket.encode(packet)
    bytes[4] = 99
    assertNull(DiscoveryPacket.decode(bytes))
  }
}
