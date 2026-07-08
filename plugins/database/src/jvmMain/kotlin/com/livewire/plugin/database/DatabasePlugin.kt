package com.livewire.plugin.database

import androidx.compose.runtime.Composable
import com.livewire.plugin.database.data.JvmDatabaseInspector
import com.livewire.plugin.database.ui.Database
import com.livewire.plugin.database.ui.Icons
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import java.io.File

class DatabasePlugin(vararg searchPaths: String) : Plugin {
  private val inspector = JvmDatabaseInspector(
    searchDirectories = searchPaths.map { File(it) },
  )

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    icon = Icons.Database,
    title = "Database",
  )

  @Composable
  override fun Content() {
    DatabasePluginContent(inspector)
  }
}
