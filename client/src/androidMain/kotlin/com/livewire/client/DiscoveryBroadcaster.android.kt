package com.livewire.client

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.os.Process
import com.livewire.ContextHolder
import com.livewire.discovery.DiscoveryPacket
import java.io.ByteArrayOutputStream

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
      appIcon = loadAppIcon(context),
    ),
    transport = Tcp,
  )
}


private fun loadAppIcon(context: Context): ByteArray? = runCatching {
  val drawable = context.packageManager.getApplicationIcon(context.applicationInfo)
  @SuppressLint("UseKtx")
  val bitmap = Bitmap.createBitmap(MaxAppIconSizePx, MaxAppIconSizePx, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)

  if (drawable is AdaptiveIconDrawable) {
    val overscan = MaxAppIconSizePx / 4
    listOfNotNull(drawable.background, drawable.foreground).forEach { layer ->
      layer.setBounds(-overscan, -overscan, MaxAppIconSizePx + overscan, MaxAppIconSizePx + overscan)
      layer.draw(canvas)
    }
  } else {
    drawable.setBounds(0, 0, MaxAppIconSizePx, MaxAppIconSizePx)
    drawable.draw(canvas)
  }

  val stream = ByteArrayOutputStream()
  bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
  bitmap.recycle()
  stream.toByteArray()
}.getOrNull()
