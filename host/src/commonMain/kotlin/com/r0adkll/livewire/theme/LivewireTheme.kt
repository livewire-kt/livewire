package com.r0adkll.livewire.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.ui.LocalLivewireHost
import com.r0adkll.livewire.ui.theme.LivewireTheme

@Composable
internal fun LivewireThemeContent(
  theme: LivewireTheme,
  host: LivewireHost,
  darkMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalLivewireHost provides host,
  ) {
    MaterialTheme(
      content = content,
      colorScheme = if (darkMode) theme.darkColorScheme else theme.lightColorScheme,
    )
  }
}
