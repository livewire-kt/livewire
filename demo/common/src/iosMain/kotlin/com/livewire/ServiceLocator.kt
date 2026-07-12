package com.livewire

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.livewire.app.LivewireDatabase
import com.livewire.client.LivewireClient
import com.livewire.plugin.database.DatabasePlugin
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.plugin.preferences.PreferencesPlugin
import com.livewire.plugin.recomposition.RecompositionPlugin
import com.livewire.theme.CustomLivewireTheme
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

object ServiceLocator {

  val settingsDataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath {
      val documents = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true,
      ).first() as String
      "$documents/demo.preferences_pb".toPath()
    }
  }

  val livewireClient by lazy {
    seedDemoPrefs()
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin())
      install(NetworkPlugin())
      install(PlaygroundPlugin())
      install(PreferencesPlugin { dataStore("settings", settingsDataStore) })
      install(RecompositionPlugin())
      debugLogging(true)
    }
  }

  private fun seedDemoPrefs() {
    val defaults = NSUserDefaults.standardUserDefaults
    if (defaults.objectForKey("username") == null) {
      defaults.setObject("rick.sanchez", forKey = "username")
      defaults.setInteger(42, forKey = "launch_count")
      defaults.setBool(true, forKey = "onboarding_complete")
    }
  }

  val database by lazy {
    val driver = NativeSqliteDriver(
      LivewireDatabase.Schema.synchronous(),
      "livewire.db",
    )

    LivewireDatabase(driver)
  }
}
