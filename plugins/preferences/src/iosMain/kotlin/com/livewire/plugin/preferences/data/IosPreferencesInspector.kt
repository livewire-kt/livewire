package com.livewire.plugin.preferences.data

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

class IosPreferencesInspector(
  suiteNames: List<String>,
  registered: List<PreferenceStore>,
) : PreferencesInspector {

  private val stores: List<PreferenceStore> = buildList {
    val bundleId = NSBundle.mainBundle.bundleIdentifier
    if (bundleId != null) {
      add(
        NsUserDefaultsStore(
          name = "standard",
          defaults = NSUserDefaults.standardUserDefaults,
          domainName = bundleId,
        ),
      )
    }
    suiteNames.forEach { suite ->
      add(
        NsUserDefaultsStore(
          name = suite,
          defaults = NSUserDefaults(suiteName = suite),
          domainName = suite,
        ),
      )
    }
    addAll(registered)
  }

  override suspend fun discoverStores(): Result<List<PreferenceStore>> = Result.success(stores)
}
