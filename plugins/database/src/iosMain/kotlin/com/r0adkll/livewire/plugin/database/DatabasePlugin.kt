package com.r0adkll.livewire.plugin.database

import androidx.compose.runtime.Composable
import com.r0adkll.livewire.plugin.database.data.IosDatabaseInspector
import com.r0adkll.livewire.plugin.database.ui.Icons
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo

class DatabasePlugin : Plugin {

  private val inspector = IosDatabaseInspector()

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    iconData = Icons.Database,
    title = "Database",
  )

  @Composable
  override fun Content() {
    DatabasePluginContent(inspector)
  }
}
