package com.r0adkll.livewire

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer

class ContextHolder : Initializer<ContextHolder> {
  override fun create(context: Context): ContextHolder {
    appContext = context.applicationContext
    return this
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

  companion object {
    @SuppressLint("StaticFieldLeak")
    lateinit var appContext: Context
  }
}
