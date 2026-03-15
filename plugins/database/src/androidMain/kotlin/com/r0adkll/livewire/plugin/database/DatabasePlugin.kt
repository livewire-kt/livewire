package com.r0adkll.livewire.plugin.database

import android.content.Context
import androidx.compose.runtime.Composable
import com.r0adkll.livewire.plugin.database.data.AndroidDatabaseInspector
import com.r0adkll.livewire.plugin.database.ui.Icons
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo

class DatabasePlugin(context: Context) : Plugin {

  private val inspector = AndroidDatabaseInspector(context)

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
