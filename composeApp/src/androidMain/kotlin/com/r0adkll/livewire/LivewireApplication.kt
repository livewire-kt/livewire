package com.r0adkll.livewire

import android.app.Application

class LivewireApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    // Poor man's "DI"
    ServiceLocator.context = this
  }
}
