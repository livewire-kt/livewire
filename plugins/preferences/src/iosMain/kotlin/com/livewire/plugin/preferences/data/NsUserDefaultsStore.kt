package com.livewire.plugin.preferences.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification
import platform.Foundation.dataWithBytes
import platform.posix.memcpy

/**
 * Wraps an [NSUserDefaults] instance. Value types are best-effort:
 * `NSNumber` does not preserve the exact Kotlin type, so integers surface
 * as Long and floating-point values as Double. Change notifications only
 * fire for in-process writes.
 */
internal class NsUserDefaultsStore(
  override val name: String,
  private val defaults: NSUserDefaults,
  private val domainName: String,
) : PreferenceStore {

  override val id: String = "nsuserdefaults:$name"
  override val kind: StoreKind = StoreKind.NsUserDefaults
  override val supportedTypes: Set<PreferenceValueType> = setOf(
    PreferenceValueType.String,
    PreferenceValueType.Long,
    PreferenceValueType.Double,
    PreferenceValueType.Boolean,
    PreferenceValueType.StringSet,
    PreferenceValueType.Bytes,
  )

  override fun entries(): Flow<List<PreferenceEntry>> = callbackFlow {
    val observer = NSNotificationCenter.defaultCenter.addObserverForName(
      name = NSUserDefaultsDidChangeNotification,
      `object` = defaults,
      queue = null,
    ) { _ ->
      trySend(snapshot())
    }
    send(snapshot())
    awaitClose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
  }.conflate()

  private fun snapshot(): List<PreferenceEntry> {
    // persistentDomainForName scopes the snapshot to this app/suite;
    // dictionaryRepresentation() would drown it in NSGlobalDomain noise.
    val domain = defaults.persistentDomainForName(domainName).orEmpty()
    return domain.entries
      .mapNotNull { (key, value) ->
        val name = key as? String ?: return@mapNotNull null
        PreferenceEntry(name, value.toPreferenceValue())
      }
      .sortedBy { it.key }
  }

  override suspend fun put(key: String, value: PreferenceValue): Result<Unit> = runCatching {
    when (value) {
      is PreferenceValue.StringValue -> defaults.setObject(value.value, forKey = key)
      is PreferenceValue.LongValue -> defaults.setInteger(value.value, forKey = key)
      is PreferenceValue.DoubleValue -> defaults.setDouble(value.value, forKey = key)
      is PreferenceValue.BooleanValue -> defaults.setBool(value.value, forKey = key)
      is PreferenceValue.IntValue -> defaults.setInteger(value.value.toLong(), forKey = key)
      is PreferenceValue.FloatValue -> defaults.setDouble(value.value.toDouble(), forKey = key)
      is PreferenceValue.StringSetValue -> defaults.setObject(value.value.toList(), forKey = key)
      is PreferenceValue.BytesValue -> defaults.setObject(value.value.toNSData(), forKey = key)
      is PreferenceValue.OpaqueValue -> error("Opaque values are not editable")
    }
  }

  override suspend fun remove(key: String): Result<Unit> = runCatching {
    defaults.removeObjectForKey(key)
  }

  override suspend fun clear(): Result<Unit> = runCatching {
    // Removing keys individually preserves any registered defaults, unlike
    // removePersistentDomainForName.
    defaults.persistentDomainForName(domainName)
      .orEmpty()
      .keys
      .filterIsInstance<String>()
      .forEach { defaults.removeObjectForKey(it) }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Any?.toPreferenceValue(): PreferenceValue = when (this) {
  is String -> PreferenceValue.StringValue(this)
  is NSNumber -> when (objCType?.toKString()) {
    "c", "B" -> PreferenceValue.BooleanValue(boolValue)
    "f" -> PreferenceValue.DoubleValue(floatValue.toDouble())
    "d" -> PreferenceValue.DoubleValue(doubleValue)
    else -> PreferenceValue.LongValue(longLongValue)
  }
  is NSData -> PreferenceValue.BytesValue(toByteArray())
  is NSArray -> {
    val items = (0uL until count).map { objectAtIndex(it) }
    if (items.all { it is String }) {
      PreferenceValue.StringSetValue(items.filterIsInstance<String>().toSet())
    } else {
      PreferenceValue.OpaqueValue(description ?: items.toString())
    }
  }
  null -> PreferenceValue.OpaqueValue("null")
  else -> PreferenceValue.OpaqueValue(toString())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
  val size = length.toInt()
  if (size == 0) return ByteArray(0)
  return ByteArray(size).apply {
    usePinned { pinned ->
      memcpy(pinned.addressOf(0), bytes, length)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
  if (isEmpty()) return NSData()
  return usePinned { pinned ->
    NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
  }
}
