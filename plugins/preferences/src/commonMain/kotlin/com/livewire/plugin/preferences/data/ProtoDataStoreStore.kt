package com.livewire.plugin.preferences.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A read-only view of an arbitrary typed [DataStore]. The content is
 * rendered as a single opaque entry via [render] (JSON recommended).
 */
internal class ProtoDataStoreStore<T>(
  override val name: String,
  private val dataStore: DataStore<T>,
  private val render: (T) -> String,
) : PreferenceStore {

  override val id: String = "proto:$name"
  override val kind: StoreKind = StoreKind.ProtoDataStore
  override val editable: Boolean = false
  override val supportedTypes: Set<PreferenceValueType> = emptySet()

  override fun entries(): Flow<List<PreferenceEntry>> = dataStore.data.map { value ->
    listOf(PreferenceEntry("content", PreferenceValue.OpaqueValue(render(value))))
  }

  override suspend fun put(key: String, value: PreferenceValue): Result<Unit> = readOnlyFailure()

  override suspend fun remove(key: String): Result<Unit> = readOnlyFailure()

  override suspend fun clear(): Result<Unit> = readOnlyFailure()

  private fun readOnlyFailure(): Result<Unit> =
    Result.failure(UnsupportedOperationException("'$name' is read-only"))
}
