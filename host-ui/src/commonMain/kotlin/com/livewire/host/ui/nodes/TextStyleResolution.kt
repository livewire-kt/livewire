package com.livewire.host.ui.nodes

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.livewire.ui.text.LivewireTextStyle
import com.livewire.ui.text.TypographyToken
import com.livewire.ui.text.toOverrideTextStyle

/**
 * Resolves a [LivewireTextStyle] against the host theme: the token picks the base from
 * [MaterialTheme.typography] (or [LocalTextStyle] when absent), then the style's specified
 * fields are merged on top.
 */
internal val LivewireTextStyle?.asComposeTextStyle: TextStyle
  @Composable get() {
    if (this == null) return LocalTextStyle.current
    val base = when (token) {
      TypographyToken.DisplayLarge -> MaterialTheme.typography.displayLarge
      TypographyToken.DisplayMedium -> MaterialTheme.typography.displayMedium
      TypographyToken.DisplaySmall -> MaterialTheme.typography.displaySmall
      TypographyToken.HeadlineLarge -> MaterialTheme.typography.headlineLarge
      TypographyToken.HeadlineMedium -> MaterialTheme.typography.headlineMedium
      TypographyToken.HeadlineSmall -> MaterialTheme.typography.headlineSmall
      TypographyToken.TitleLarge -> MaterialTheme.typography.titleLarge
      TypographyToken.TitleMedium -> MaterialTheme.typography.titleMedium
      TypographyToken.TitleSmall -> MaterialTheme.typography.titleSmall
      TypographyToken.BodyLarge -> MaterialTheme.typography.bodyLarge
      TypographyToken.BodyMedium -> MaterialTheme.typography.bodyMedium
      TypographyToken.BodySmall -> MaterialTheme.typography.bodySmall
      TypographyToken.LabelLarge -> MaterialTheme.typography.labelLarge
      TypographyToken.LabelMedium -> MaterialTheme.typography.labelMedium
      TypographyToken.LabelSmall -> MaterialTheme.typography.labelSmall
      null -> LocalTextStyle.current
    }
    return base.merge(toOverrideTextStyle())
  }
