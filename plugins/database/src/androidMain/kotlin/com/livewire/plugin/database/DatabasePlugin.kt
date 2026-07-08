package com.livewire.plugin.database

import android.content.Context
import androidx.compose.runtime.Composable
import com.livewire.plugin.database.data.AndroidDatabaseInspector
import com.livewire.plugin.database.ui.Database
import com.livewire.plugin.database.ui.Icons
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo

class DatabasePlugin(context: Context) : Plugin {

  private val inspector = AndroidDatabaseInspector(context)

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
