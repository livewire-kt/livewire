package com.livewire.ui.theme

import com.livewire.ui.text.LivewireTextStyle
import com.livewire.ui.text.TypographyToken

/**
 * The Material3 typography tokens as [LivewireTextStyle]s, accessible via
 * `LivewireTheme.typography`.
 *
 * Token styles carry no metric values on the client — the host's `MaterialTheme.typography`
 * is the source of truth, so text automatically matches the host theme. Use `.copy(...)`
 * to override individual properties on top of a token.
 */
object LivewireTypography {
  val displayLarge = LivewireTextStyle(token = TypographyToken.DisplayLarge)
  val displayMedium = LivewireTextStyle(token = TypographyToken.DisplayMedium)
  val displaySmall = LivewireTextStyle(token = TypographyToken.DisplaySmall)
  val headlineLarge = LivewireTextStyle(token = TypographyToken.HeadlineLarge)
  val headlineMedium = LivewireTextStyle(token = TypographyToken.HeadlineMedium)
  val headlineSmall = LivewireTextStyle(token = TypographyToken.HeadlineSmall)
  val titleLarge = LivewireTextStyle(token = TypographyToken.TitleLarge)
  val titleMedium = LivewireTextStyle(token = TypographyToken.TitleMedium)
  val titleSmall = LivewireTextStyle(token = TypographyToken.TitleSmall)
  val bodyLarge = LivewireTextStyle(token = TypographyToken.BodyLarge)
  val bodyMedium = LivewireTextStyle(token = TypographyToken.BodyMedium)
  val bodySmall = LivewireTextStyle(token = TypographyToken.BodySmall)
  val labelLarge = LivewireTextStyle(token = TypographyToken.LabelLarge)
  val labelMedium = LivewireTextStyle(token = TypographyToken.LabelMedium)
  val labelSmall = LivewireTextStyle(token = TypographyToken.LabelSmall)
}
