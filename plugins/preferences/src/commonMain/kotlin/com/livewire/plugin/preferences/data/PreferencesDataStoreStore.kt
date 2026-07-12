package com.livewire.plugin.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PreferencesDataStoreStore(
  override val name: String,
  private val dataStore: DataStore<Preferences>,
) : PreferenceStore {

  override val id: String = "datastore:$name"
  override val kind: StoreKind = StoreKind.PreferencesDataStore
  override val supportedTypes: Set<PreferenceValueType> = PreferenceValueType.entries.toSet()

  override fun entries(): Flow<List<PreferenceEntry>> = dataStore.data.map { preferences ->
    preferences.asMap()
      .map { (key, value) -> PreferenceEntry(key.name, value.toPreferenceValue()) }
      .sortedBy { it.key }
  }

  override suspend fun put(key: String, value: PreferenceValue): Result<Unit> = edit { preferences ->
    when (value) {
      is PreferenceValue.StringValue -> preferences[stringPreferencesKey(key)] = value.value
      is PreferenceValue.IntValue -> preferences[intPreferencesKey(key)] = value.value
      is PreferenceValue.LongValue -> preferences[longPreferencesKey(key)] = value.value
      is PreferenceValue.FloatValue -> preferences[floatPreferencesKey(key)] = value.value
      is PreferenceValue.DoubleValue -> preferences[doublePreferencesKey(key)] = value.value
      is PreferenceValue.BooleanValue -> preferences[booleanPreferencesKey(key)] = value.value
      is PreferenceValue.StringSetValue -> preferences[stringSetPreferencesKey(key)] = value.value
      is PreferenceValue.BytesValue -> preferences[byteArrayPreferencesKey(key)] = value.value
      is PreferenceValue.OpaqueValue -> error("Opaque values are not editable")
    }
  }

  // Preferences.Key equality is name-based, so any typed key removes the entry
  override suspend fun remove(key: String): Result<Unit> = edit { it.remove(stringPreferencesKey(key)) }

  override suspend fun clear(): Result<Unit> = edit { it.clear() }

  private suspend fun edit(block: (MutablePreferences) -> Unit): Result<Unit> =
    runCatching { dataStore.edit(block) }.map {}
}

private fun Any.toPreferenceValue(): PreferenceValue = when (this) {
  is Boolean -> PreferenceValue.BooleanValue(this)
  is Int -> PreferenceValue.IntValue(this)
  is Long -> PreferenceValue.LongValue(this)
  is Float -> PreferenceValue.FloatValue(this)
  is Double -> PreferenceValue.DoubleValue(this)
  is String -> PreferenceValue.StringValue(this)
  is ByteArray -> PreferenceValue.BytesValue(this)
  is Set<*> -> PreferenceValue.StringSetValue(filterIsInstance<String>().toSet())
  else -> PreferenceValue.OpaqueValue(toString())
}
