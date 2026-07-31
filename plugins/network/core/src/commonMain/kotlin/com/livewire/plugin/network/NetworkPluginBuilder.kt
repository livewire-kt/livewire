package com.livewire.plugin.network

import com.livewire.ui.PluginBuilderDsl

@PluginBuilderDsl
class NetworkPluginBuilder internal constructor() {

  var requestPathMaxLines: Int = DEFAULT_MAX_LINES

  companion object {
    private const val DEFAULT_MAX_LINES = 3
  }
}
