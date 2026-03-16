package com.r0adkll.livewire.plugin.database

import androidx.compose.runtime.Composable
import com.r0adkll.livewire.plugin.database.data.JvmDatabaseInspector
import com.r0adkll.livewire.plugin.database.ui.Icons
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import java.io.File

class DatabasePlugin(vararg searchPaths: String) : Plugin {
  private val inspector = JvmDatabaseInspector(
    searchDirectories = searchPaths.map { File(it) },
  )

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
