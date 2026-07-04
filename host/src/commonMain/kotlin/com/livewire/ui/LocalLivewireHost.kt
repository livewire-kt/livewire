package com.livewire.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.livewire.runtime.LivewireHost

val LocalLivewireHost = staticCompositionLocalOf<LivewireHost> {
  error("LivewireHost is not configured in this composition!")
}
