package com.livewire.plugin.preferences.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPreferencesInspector(
  private val context: Context,
  private val registered: List<PreferenceStore>,
) : PreferencesInspector {

  private val cache = mutableMapOf<String, PreferenceStore>()

  override suspend fun discoverStores(): Result<List<PreferenceStore>> = withContext(Dispatchers.IO) {
    runCatching {
      val sharedPrefsDir = File(context.dataDir, "shared_prefs")
      val discovered = sharedPrefsDir.listFiles { file -> file.name.endsWith(".xml") }
        .orEmpty()
        .map { it.name.removeSuffix(".xml") }
        .sorted()
        .map { name ->
          cache.getOrPut("sharedprefs:$name") {
            SharedPreferencesStore(name, context.getSharedPreferences(name, Context.MODE_PRIVATE))
          }
        }

      discovered + registered
    }
  }
}
