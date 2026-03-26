package com.r0adkll.livewire.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences
import kotlinx.coroutines.CoroutineScope

class LivewireSettings(
  override val scope: CoroutineScope,
  override val settings: ObservableSettings = PreferencesSettings(
    Preferences.userRoot().node("com.r0adkll.livewire.host"),
  ),
) : AppSettings() {

  var darkMode by booleanSetting(KEY_DARK_MODE, defaultValue = false)
  var lastConnectedApp by stringOrNullSetting(KEY_LAST_CONNECTED_APP)
  var menuExpanded by booleanSetting(KEY_MENU_EXPANDED, defaultValue = true)

  var windowWidth by intSetting(KEY_WINDOW_WIDTH, defaultValue = DEFAULT_WINDOW_WIDTH)
  var windowHeight by intSetting(KEY_WINDOW_HEIGHT, defaultValue = DEFAULT_WINDOW_HEIGHT)
  var windowX by intSetting(KEY_WINDOW_X, defaultValue = Int.MIN_VALUE)
  var windowY by intSetting(KEY_WINDOW_Y, defaultValue = Int.MIN_VALUE)

  val hasWindowPosition: Boolean
    get() = windowX != Int.MIN_VALUE && windowY != Int.MIN_VALUE

  companion object {
    const val DEFAULT_WINDOW_WIDTH = 1200
    const val DEFAULT_WINDOW_HEIGHT = 800

    const val KEY_DARK_MODE = "pref_dark_mode"
    const val KEY_LAST_CONNECTED_APP = "pref_last_connected_app"
    const val KEY_MENU_EXPANDED = "pref_menu_expanded"
    const val KEY_WINDOW_WIDTH = "pref_window_width"
    const val KEY_WINDOW_HEIGHT = "pref_window_height"
    const val KEY_WINDOW_X = "pref_window_x"
    const val KEY_WINDOW_Y = "pref_window_y"
  }
}

@Composable
internal fun rememberLivewireSettings(): LivewireSettings {
  val scope = rememberCoroutineScope()
  return remember {
    LivewireSettings(scope)
  }
}
