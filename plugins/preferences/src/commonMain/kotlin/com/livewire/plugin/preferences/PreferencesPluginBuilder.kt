package com.livewire.plugin.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.livewire.plugin.preferences.data.PreferenceStore
import com.livewire.plugin.preferences.data.PreferencesDataStoreStore
import com.livewire.plugin.preferences.data.ProtoDataStoreStore

@DslMarker
annotation class PreferencesPluginDsl

/**
 * Registers stores the inspector cannot discover on its own. DataStore
 * enforces a single instance per file, so the app hands the inspector its
 * live instances rather than the inspector opening files itself.
 */
@PreferencesPluginDsl
class PreferencesPluginBuilder internal constructor() {

  internal val stores = mutableListOf<PreferenceStore>()

  /** Registers a Preferences DataStore for full read/write inspection. */
  fun dataStore(name: String, dataStore: DataStore<Preferences>) {
    stores += PreferencesDataStoreStore(name, dataStore)
  }

  /**
   * Registers any typed [DataStore] for read-only inspection, rendered by
   * [render] (JSON recommended).
   */
  fun <T> protoDataStore(name: String, dataStore: DataStore<T>, render: (T) -> String = { it.toString() }) {
    stores += ProtoDataStoreStore(name, dataStore, render)
  }
}
