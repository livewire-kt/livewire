package com.livewire.plugin.preferences.data

interface PreferencesInspector {
  /**
   * Returns the platform-discovered stores plus any app-registered stores.
   * The same id must always map to the same [PreferenceStore] instance so
   * active [PreferenceStore.entries] subscriptions stay valid across
   * refreshes.
   */
  suspend fun discoverStores(): Result<List<PreferenceStore>>
}
