package com.livewire.plugin.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import com.livewire.plugin.preferences.data.AndroidPreferencesInspector
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.plugin.preferences.ui.Tune
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo

class PreferencesPlugin(
  context: Context,
  configure: PreferencesPluginBuilder.() -> Unit = {},
) : Plugin {

  private val inspector = AndroidPreferencesInspector(
    context = context.applicationContext,
    registered = PreferencesPluginBuilder().apply(configure).stores,
  )

  override val info: PluginInfo = PluginInfo(
    pluginId = "preferences",
    icon = Icons.Tune,
    title = "Preferences",
  )

  @Composable
  override fun Content() {
    PreferencesPluginContent(inspector)
  }
}
