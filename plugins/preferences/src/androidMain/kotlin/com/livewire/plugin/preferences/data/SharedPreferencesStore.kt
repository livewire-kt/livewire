package com.livewire.plugin.preferences.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class SharedPreferencesStore(
  override val name: String,
  private val prefs: SharedPreferences,
) : PreferenceStore {

  override val id: String = "sharedprefs:$name"
  override val kind: StoreKind = StoreKind.SharedPreferences
  override val supportedTypes: Set<PreferenceValueType> = setOf(
    PreferenceValueType.String,
    PreferenceValueType.Int,
    PreferenceValueType.Long,
    PreferenceValueType.Float,
    PreferenceValueType.Boolean,
    PreferenceValueType.StringSet,
  )

  override fun entries(): Flow<List<PreferenceEntry>> = callbackFlow {
    // The framework holds listeners in a WeakHashMap; this local is the
    // strong reference keeping it alive until awaitClose. The key param is
    // ignored because it is null when clear() fires the listener.
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
      trySend(snapshot())
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)
    send(snapshot())
    awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
  }.conflate().flowOn(Dispatchers.IO)

  private fun snapshot(): List<PreferenceEntry> = prefs.all.entries
    .sortedBy { it.key }
    .map { (key, value) ->
      PreferenceEntry(
        key = key,
        value = when (value) {
          is Boolean -> PreferenceValue.BooleanValue(value)
          is Int -> PreferenceValue.IntValue(value)
          is Long -> PreferenceValue.LongValue(value)
          is Float -> PreferenceValue.FloatValue(value)
          is String -> PreferenceValue.StringValue(value)
          is Set<*> -> PreferenceValue.StringSetValue(value.filterIsInstance<String>().toSet())
          null -> PreferenceValue.OpaqueValue("null")
          else -> PreferenceValue.OpaqueValue(value.toString())
        },
      )
    }

  override suspend fun put(key: String, value: PreferenceValue): Result<Unit> = edit { editor ->
    when (value) {
      is PreferenceValue.StringValue -> editor.putString(key, value.value)
      is PreferenceValue.IntValue -> editor.putInt(key, value.value)
      is PreferenceValue.LongValue -> editor.putLong(key, value.value)
      is PreferenceValue.FloatValue -> editor.putFloat(key, value.value)
      is PreferenceValue.BooleanValue -> editor.putBoolean(key, value.value)
      is PreferenceValue.StringSetValue -> editor.putStringSet(key, value.value)
      is PreferenceValue.DoubleValue,
      is PreferenceValue.BytesValue,
      is PreferenceValue.OpaqueValue,
      -> error("SharedPreferences does not support ${value::class.simpleName}")
    }
  }

  override suspend fun remove(key: String): Result<Unit> = edit { it.remove(key) }

  override suspend fun clear(): Result<Unit> = edit { it.clear() }

  private suspend fun edit(block: (SharedPreferences.Editor) -> Unit): Result<Unit> =
    withContext(Dispatchers.IO) {
      runCatching {
        val editor = prefs.edit()
        block(editor)
        check(editor.commit()) { "Failed to write to '$name'" }
      }
    }
}
