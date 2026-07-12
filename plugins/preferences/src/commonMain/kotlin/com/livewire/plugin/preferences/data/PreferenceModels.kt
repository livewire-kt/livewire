package com.livewire.plugin.preferences.data

import androidx.compose.runtime.Immutable
import kotlin.io.encoding.Base64

enum class PreferenceValueType {
  String,
  Int,
  Long,
  Float,
  Double,
  Boolean,
  StringSet,
  Bytes,
}

sealed interface PreferenceValue {
  data class StringValue(val value: String) : PreferenceValue
  data class IntValue(val value: Int) : PreferenceValue
  data class LongValue(val value: Long) : PreferenceValue
  data class FloatValue(val value: Float) : PreferenceValue
  data class DoubleValue(val value: Double) : PreferenceValue
  data class BooleanValue(val value: Boolean) : PreferenceValue
  data class StringSetValue(val value: Set<String>) : PreferenceValue

  class BytesValue(val value: ByteArray) : PreferenceValue {
    override fun equals(other: Any?): Boolean =
      other is BytesValue && value.contentEquals(other.value)

    override fun hashCode(): Int = value.contentHashCode()
  }

  /** A display-only value for content the inspector can render but not edit. */
  data class OpaqueValue(val rendered: String) : PreferenceValue
}

/** The editable type of this value, or null if it is display-only. */
val PreferenceValue.type: PreferenceValueType?
  get() = when (this) {
    is PreferenceValue.StringValue -> PreferenceValueType.String
    is PreferenceValue.IntValue -> PreferenceValueType.Int
    is PreferenceValue.LongValue -> PreferenceValueType.Long
    is PreferenceValue.FloatValue -> PreferenceValueType.Float
    is PreferenceValue.DoubleValue -> PreferenceValueType.Double
    is PreferenceValue.BooleanValue -> PreferenceValueType.Boolean
    is PreferenceValue.StringSetValue -> PreferenceValueType.StringSet
    is PreferenceValue.BytesValue -> PreferenceValueType.Bytes
    is PreferenceValue.OpaqueValue -> null
  }

val PreferenceValue.editable: Boolean
  get() = this !is PreferenceValue.OpaqueValue

/**
 * Renders this value as the text shown in read mode and seeded into the
 * editor. [parsePreferenceValue] accepts everything this produces.
 */
fun PreferenceValue.render(): String = when (this) {
  is PreferenceValue.StringValue -> value
  is PreferenceValue.IntValue -> value.toString()
  is PreferenceValue.LongValue -> value.toString()
  is PreferenceValue.FloatValue -> value.toString()
  is PreferenceValue.DoubleValue -> value.toString()
  is PreferenceValue.BooleanValue -> value.toString()
  is PreferenceValue.StringSetValue -> value.joinToString("\n")
  is PreferenceValue.BytesValue -> Base64.encode(value)
  is PreferenceValue.OpaqueValue -> rendered
}

/**
 * Parses editor text into a typed value. String sets are one entry per line
 * (blank lines dropped); bytes are Base64.
 */
fun parsePreferenceValue(type: PreferenceValueType, text: String): Result<PreferenceValue> = when (type) {
  PreferenceValueType.String -> Result.success(PreferenceValue.StringValue(text))

  PreferenceValueType.Int -> text.trim().toIntOrNull()
    ?.let { Result.success(PreferenceValue.IntValue(it)) }
    ?: parseFailure(text, "Int")

  PreferenceValueType.Long -> text.trim().toLongOrNull()
    ?.let { Result.success(PreferenceValue.LongValue(it)) }
    ?: parseFailure(text, "Long")

  PreferenceValueType.Float -> text.trim().toFloatOrNull()
    ?.let { Result.success(PreferenceValue.FloatValue(it)) }
    ?: parseFailure(text, "Float")

  PreferenceValueType.Double -> text.trim().toDoubleOrNull()
    ?.let { Result.success(PreferenceValue.DoubleValue(it)) }
    ?: parseFailure(text, "Double")

  PreferenceValueType.Boolean -> when (text.trim().lowercase()) {
    "true" -> Result.success(PreferenceValue.BooleanValue(true))
    "false" -> Result.success(PreferenceValue.BooleanValue(false))
    else -> parseFailure(text, "Boolean")
  }

  PreferenceValueType.StringSet -> Result.success(
    PreferenceValue.StringSetValue(
      text.lines().map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
    ),
  )

  PreferenceValueType.Bytes -> runCatching {
    PreferenceValue.BytesValue(Base64.decode(text.trim()))
  }.recoverCatching {
    throw IllegalArgumentException("Not valid Base64: '$text'")
  }
}

private fun parseFailure(text: String, type: String): Result<PreferenceValue> =
  Result.failure(IllegalArgumentException("'$text' is not a valid $type"))

data class PreferenceEntry(
  val key: String,
  val value: PreferenceValue,
)

enum class StoreKind(val label: String) {
  SharedPreferences("SharedPreferences"),
  PreferencesDataStore("DataStore"),
  NsUserDefaults("NSUserDefaults"),
  JavaPreferences("Java Preferences"),
  ProtoDataStore("Proto DataStore"),
}

/** A snapshot of a [PreferenceStore]'s identity, safe to hold in UI state. */
@Immutable
data class StoreInfo(
  val id: String,
  val name: String,
  val kind: StoreKind,
  val editable: Boolean,
  val supportedTypes: Set<PreferenceValueType>,
)
