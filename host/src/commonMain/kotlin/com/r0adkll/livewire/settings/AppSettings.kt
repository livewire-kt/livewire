package com.r0adkll.livewire.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalSettingsApi::class)
abstract class AppSettings {
  abstract val scope: CoroutineScope
  abstract val settings: ObservableSettings

  fun booleanSetting(key: String, defaultValue: Boolean = false) = object : SettingsProperty<AppSettings, Boolean> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): Boolean {
      return settings.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: Boolean) {
      settings.putBoolean(key, value)
    }

    override fun observe(): StateFlow<Boolean> {
      return settings.getBooleanFlow(key, defaultValue)
        .stateIn(scope, SharingStarted.Lazily, settings.getBoolean(key, defaultValue))
    }
  }

  fun intSetting(key: String, defaultValue: Int = 0) = object : SettingsProperty<AppSettings, Int> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): Int {
      return settings.getInt(key, defaultValue)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: Int) {
      settings.putInt(key, value)
    }

    override fun observe(): StateFlow<Int> {
      return settings.getIntFlow(key, defaultValue)
        .stateIn(scope, SharingStarted.Lazily, settings.getInt(key, defaultValue))
    }
  }

  fun longSetting(key: String, defaultValue: Long = 0L) = object : SettingsProperty<AppSettings, Long> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): Long {
      return settings.getLong(key, defaultValue)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: Long) {
      settings.putLong(key, value)
    }

    override fun observe(): StateFlow<Long> {
      return settings.getLongFlow(key, defaultValue)
        .stateIn(scope, SharingStarted.Lazily, settings.getLong(key, defaultValue))
    }
  }

  fun floatSetting(key: String, defaultValue: Float = 0f) = object : SettingsProperty<AppSettings, Float> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): Float {
      return settings.getFloat(key, defaultValue)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: Float) {
      settings.putFloat(key, value)
    }

    override fun observe(): StateFlow<Float> {
      return settings.getFloatFlow(key, defaultValue)
        .stateIn(scope, SharingStarted.Lazily, settings.getFloat(key, defaultValue))
    }
  }

  fun stringSetting(key: String, defaultValue: String = "") = object : SettingsProperty<AppSettings, String> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): String {
      return settings.getString(key, defaultValue)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: String) {
      settings.putString(key, value)
    }

    override fun observe(): StateFlow<String> {
      return settings.getStringFlow(key, defaultValue)
        .stateIn(scope, SharingStarted.Lazily, settings.getString(key, defaultValue))
    }
  }

  fun stringOrNullSetting(key: String) = object : SettingsProperty<AppSettings, String?> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): String? {
      return settings.getStringOrNull(key)
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: String?) {
      if (value == null) {
        settings.remove(key)
      } else {
        settings.putString(key, value)
      }
    }

    override fun observe(): StateFlow<String?> {
      return settings.getStringOrNullFlow(key)
        .stateIn(scope, SharingStarted.Lazily, settings.getStringOrNull(key))
    }
  }

  inline fun <reified T : Enum<T>> enumSetting(
    key: String,
    defaultValue: T,
  ) = object : SettingsProperty<AppSettings, T> {
    override fun getValue(thisRef: AppSettings, property: KProperty<*>): T {
      return settings.getStringOrNull(key)
        ?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }
        ?: defaultValue
    }

    override fun setValue(thisRef: AppSettings, property: KProperty<*>, value: T) {
      settings.putString(key, value.name)
    }

    override fun observe(): StateFlow<T> {
      val currentValue = settings.getStringOrNull(key)
        ?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }
        ?: defaultValue
      return settings.getStringOrNullFlow(key).map { raw ->
        raw?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: defaultValue
      }.stateIn(scope, SharingStarted.Lazily, currentValue)
    }
  }

  interface SettingsProperty<in T : AppSettings, V> : ReadWriteProperty<T, V> {
    fun observe(): StateFlow<V>
  }
}

@Suppress("UNCHECKED_CAST")
fun <T> KMutableProperty0<T>.observe(): StateFlow<T> {
  isAccessible = true
  return (getDelegate() as AppSettings.SettingsProperty<AppSettings, T>).observe()
}
