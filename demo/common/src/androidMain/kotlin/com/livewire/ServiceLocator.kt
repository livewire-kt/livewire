package com.livewire

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.livewire.app.LivewireDatabase
import com.livewire.client.LivewireClient
import com.livewire.plugin.database.DatabasePlugin
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.plugin.preferences.PreferencesPlugin
import com.livewire.plugin.recomposition.RecompositionPlugin
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.theme.CustomLivewireTheme
import okio.Path.Companion.toPath

@SuppressLint("StaticFieldLeak")
object ServiceLocator {
  lateinit var context: Context

  val settingsDataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath {
      context.filesDir.resolve("demo.preferences_pb").absolutePath.toPath()
    }
  }

  val livewireClient by lazy {
    seedDemoPrefs(context)
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin(context))
      install(NetworkPlugin())
      install(PlaygroundPlugin())
      install(PreferencesPlugin(context) { dataStore("settings", settingsDataStore) })
      install(RecompositionPlugin())

      layoutNodeSerialization(LayoutNodeSerialization.Json)
      debugLogging(true)
    }
  }

  private fun seedDemoPrefs(context: Context) {
    val prefs = context.getSharedPreferences("demo_prefs", Context.MODE_PRIVATE)
    if (prefs.all.isEmpty()) {
      prefs.edit()
        .putString("username", "rick.sanchez")
        .putInt("launch_count", 42)
        .putBoolean("onboarding_complete", true)
        .putFloat("playback_speed", 1.5f)
        .putStringSet("favorite_characters", setOf("Rick", "Morty", "Birdperson"))
        .apply()
    }
  }

  val database by lazy {
    val driver = AndroidSqliteDriver(
      LivewireDatabase.Schema.synchronous(),
      context,
      "livewire.db",
    )

    LivewireDatabase(driver)
  }
}
