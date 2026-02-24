package com.r0adkll.livewire.ui.actions

import androidx.compose.runtime.staticCompositionLocalOf

interface LivewireActionDispatcher {
  suspend fun dispatch(action: LivewireAction)
}

val LocalLivewireActionDispatcher = staticCompositionLocalOf<LivewireActionDispatcher> {
  error("LivewireActionDispatcher not initialized")
}
