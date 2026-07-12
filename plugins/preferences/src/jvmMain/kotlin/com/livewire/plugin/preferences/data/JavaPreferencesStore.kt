package com.livewire.plugin.preferences.data

import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Wraps a `java.util.prefs` user-root node. Values are string-typed on
 * read-back, so everything surfaces (and is edited) as a String.
 */
internal class JavaPreferencesStore(
  private val nodePath: String,
) : PreferenceStore {

  private val prefs: Preferences = Preferences.userRoot().node(nodePath)

  override val id: String = "javaprefs:$nodePath"
  override val name: String = nodePath
  override val kind: StoreKind = StoreKind.JavaPreferences
  override val supportedTypes: Set<PreferenceValueType> = setOf(PreferenceValueType.String)

  override fun entries(): Flow<List<PreferenceEntry>> = callbackFlow {
    val listener = PreferenceChangeListener { trySend(snapshot()) }
    prefs.addPreferenceChangeListener(listener)
    send(snapshot())
    awaitClose { prefs.removePreferenceChangeListener(listener) }
  }.conflate().flowOn(Dispatchers.IO)

  private fun snapshot(): List<PreferenceEntry> = prefs.keys()
    .sorted()
    .map { key -> PreferenceEntry(key, PreferenceValue.StringValue(prefs.get(key, ""))) }

  override suspend fun put(key: String, value: PreferenceValue): Result<Unit> = edit {
    require(value is PreferenceValue.StringValue) {
      "Java Preferences only support String values"
    }
    prefs.put(key, value.value)
  }

  override suspend fun remove(key: String): Result<Unit> = edit { prefs.remove(key) }

  override suspend fun clear(): Result<Unit> = edit { prefs.clear() }

  private suspend fun edit(block: () -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
      block()
      prefs.flush()
    }
  }
}
