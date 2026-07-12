package com.livewire.plugin.preferences

import androidx.compose.runtime.Composable
import com.livewire.plugin.preferences.data.IosPreferencesInspector
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.plugin.preferences.ui.Tune
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo

/**
 * @param suiteNames Additional `NSUserDefaults` suites to inspect alongside
 * the standard defaults.
 */
class PreferencesPlugin(
  suiteNames: List<String> = emptyList(),
  configure: PreferencesPluginBuilder.() -> Unit = {},
) : Plugin {

  private val inspector = IosPreferencesInspector(
    suiteNames = suiteNames,
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
