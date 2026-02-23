package com.r0adkll.livewire.plugin.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.widget.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class DatabasePlugin : Plugin {

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    iconData = DatabaseIconSvgData,
    title = "Database",
  )

  @Composable
  override fun Content() {
    Column {
      val messages = remember { mutableStateListOf<String>() }
      messages.forEach { message ->
        Text(message)
      }

      LaunchedEffect(Unit) {
        var count = 0
        while (isActive) {
          delay(5.seconds)
          count++
          messages += "Messages: $count"
        }
      }
    }
  }
}

private const val DatabaseIconSvgData = "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 -960 960 960\" width=\"24px\" fill=\"#e8eaed\"><path d=\"M735-567q105-47 105-113T735-793q-105-47-255-47t-255 47q-105 47-105 113t105 113q105 47 255 47t255-47ZM582.5-428.5Q644-437 701-456t98-49.5q41-30.5 41-74.5v100q0 44-41 74.5T701-356q-57 19-118.5 27.5T480-320q-41 0-102.5-8.5T259-356q-57-19-98-49.5T120-480v-100q0 44 41 74.5t98 49.5q57 19 118.5 27.5T480-420q41 0 102.5-8.5Zm0 200Q644-237 701-256t98-49.5q41-30.5 41-74.5v100q0 44-41 74.5T701-156q-57 19-118.5 27.5T480-120q-41 0-102.5-8.5T259-156q-57-19-98-49.5T120-280v-100q0 44 41 74.5t98 49.5q57 19 118.5 27.5T480-220q41 0 102.5-8.5Z\"/></svg>"
