package com.livewire.client

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Process
import com.livewire.ContextHolder
import com.livewire.discovery.DiscoveryPacket
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64

actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  val context = ContextHolder.appContext

  return DiscoveryConfig(
    packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = context.applicationInfo.loadLabel(context.packageManager).toString(),
      packageName = context.packageName,
      processId = Process.myPid().toLong(),
      platform = Android,
      deviceName = Build.MODEL,
      osVersion = Build.VERSION.RELEASE,
      appIcon = loadAppIconBase64(context),
    ),
    transport = Tcp,
  )
}


private fun loadAppIconBase64(context: Context): String? = runCatching {
  val drawable = context.packageManager.getApplicationIcon(context.applicationInfo)
  @SuppressLint("UseKtx")
  val bitmap = Bitmap.createBitmap(AppIconSizePx, AppIconSizePx, Bitmap.Config.ARGB_8888)
  drawable.setBounds(0, 0, AppIconSizePx, AppIconSizePx)
  drawable.draw(Canvas(bitmap))

  val stream = ByteArrayOutputStream()
  bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
  bitmap.recycle()

  Base64.encode(stream.toByteArray())
}.getOrNull()

private const val AppIconSizePx = 128
