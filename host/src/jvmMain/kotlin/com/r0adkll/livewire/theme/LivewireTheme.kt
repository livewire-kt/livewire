package com.r0adkll.livewire.com.r0adkll.livewire.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.ui.LocalLivewireHost

@Composable
internal fun LivewireTheme(
  host: LivewireHost,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalLivewireHost provides host,
  ) {
    MaterialTheme(
      content = content,
    )
  }
}
