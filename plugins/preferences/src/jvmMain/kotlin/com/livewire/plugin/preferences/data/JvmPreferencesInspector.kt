package com.livewire.plugin.preferences.data

class JvmPreferencesInspector(
  nodePaths: List<String>,
  registered: List<PreferenceStore>,
) : PreferencesInspector {

  private val stores: List<PreferenceStore> =
    nodePaths.map { JavaPreferencesStore(it) } + registered

  override suspend fun discoverStores(): Result<List<PreferenceStore>> = Result.success(stores)
}
