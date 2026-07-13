package com.livewire.plugin.preferences.data

import kotlinx.coroutines.flow.Flow

/**
 * A single key-value store (a SharedPreferences file, a DataStore instance,
 * an NSUserDefaults suite, …) exposed to the preferences inspector.
 */
interface PreferenceStore {
  /** Unique across all stores, stable across refreshes, e.g. "sharedprefs:app_settings". */
  val id: String
  val name: String
  val kind: StoreKind
  val editable: Boolean get() = true

  /** The value types offered when adding a new entry to this store. */
  val supportedTypes: Set<PreferenceValueType>

  /**
   * Emits the current entries immediately, then again on every change,
   * sorted by key.
   */
  fun entries(): Flow<List<PreferenceEntry>>

  suspend fun put(key: String, value: PreferenceValue): Result<Unit>

  suspend fun remove(key: String): Result<Unit>

  suspend fun clear(): Result<Unit>
}

fun PreferenceStore.toStoreInfo(): StoreInfo = StoreInfo(
  id = id,
  name = name,
  kind = kind,
  editable = editable,
  supportedTypes = supportedTypes,
)
