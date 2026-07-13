package com.livewire.plugin.preferences

import androidx.compose.runtime.Composable
import com.livewire.plugin.preferences.data.JvmPreferencesInspector
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.plugin.preferences.ui.Tune
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo

/**
 * @param preferenceNodes `java.util.prefs` user-root node paths to inspect,
 * e.g. `"/com/example/app"`.
 */
class PreferencesPlugin(
  vararg preferenceNodes: String,
  configure: PreferencesPluginBuilder.() -> Unit = {},
) : Plugin {

  private val inspector = JvmPreferencesInspector(
    nodePaths = preferenceNodes.toList(),
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
