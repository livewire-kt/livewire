package com.r0adkll.livewire.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.r0adkll.livewire.runtime.LivewireHost

val LocalLivewireHost = staticCompositionLocalOf<LivewireHost> {
  error("LivewireHost is not configured in this composition!")
}
