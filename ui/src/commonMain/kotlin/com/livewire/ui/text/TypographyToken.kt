package com.livewire.ui.text

import kotlinx.serialization.Serializable

/**
 * A Material3 typography token. Tokens are resolved on the host against its
 * `MaterialTheme.typography`, so text using a token always matches the host theme.
 */
@Serializable
enum class TypographyToken {
  DisplayLarge,
  DisplayMedium,
  DisplaySmall,
  HeadlineLarge,
  HeadlineMedium,
  HeadlineSmall,
  TitleLarge,
  TitleMedium,
  TitleSmall,
  BodyLarge,
  BodyMedium,
  BodySmall,
  LabelLarge,
  LabelMedium,
  LabelSmall,
}
