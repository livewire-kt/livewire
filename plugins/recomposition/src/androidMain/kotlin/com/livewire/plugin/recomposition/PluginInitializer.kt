package com.livewire.plugin.recomposition

import android.content.Context
import androidx.startup.Initializer
import com.livewire.ContextHolder

class PluginInitializer : Initializer<PluginInitializer> {
  override fun create(context: Context): PluginInitializer {
    RecompositionPlugin.init()
    return this
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = listOf(ContextHolder::class.java)
}
