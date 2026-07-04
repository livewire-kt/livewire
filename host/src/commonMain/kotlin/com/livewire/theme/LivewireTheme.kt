package com.livewire.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.livewire.runtime.LivewireHost
import com.livewire.ui.LocalLivewireHost
import com.livewire.ui.theme.LivewireTheme

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
