package com.livewire.client

import com.livewire.discovery.DiscoveryPacket
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64

actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  val appName = System.getProperty("sun.java.command").orEmpty().substringBefore(" ").ifEmpty {
    Frame.getFrames()
      .asSequence()
      .filter { it.isVisible }.firstNotNullOfOrNull { it.title.takeIf(String::isNotBlank) }
      ?: UnknownConfigField
  }

  return DiscoveryConfig(
    packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = appName,
      packageName = System.getProperty("java.class.path") ?: UnknownConfigField,
      processId = ProcessHandle.current().pid(),
      platform = Desktop,
      deviceName = "Desktop",
      osVersion = System.getProperty("os.version") ?: UnknownConfigField,
      appIcon = loadAppIconBase64(),
    ),
    transport = Udp,
  )
}

private fun loadAppIconBase64(): String? = runCatching {
  val icon = Frame.getFrames()
    .asSequence()
    .flatMap { it.iconImages }
    .filter { it.getWidth(null) > 0 && it.getHeight(null) > 0 }
    .maxByOrNull { it.getWidth(null) }
    ?: return null

  val size = minOf(icon.getWidth(null), MaxAppIconSizePx)
  val bitmap = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
  bitmap.createGraphics().apply {
    drawImage(icon, 0, 0, size, size, null)
    dispose()
  }

  val stream = ByteArrayOutputStream()
  ImageIO.write(bitmap, "png", stream)

  Base64.encode(stream.toByteArray()).takeIf { it.length <= MaxAppIconBase64Length }
}.getOrNull()

private const val MaxAppIconSizePx = 64
private const val MaxAppIconBase64Length = 8_000
