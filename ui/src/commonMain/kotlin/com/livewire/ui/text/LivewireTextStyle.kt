package com.livewire.ui.text

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import com.livewire.ui.graphics.ColorSerializer
import kotlinx.serialization.Serializable

/**
 * The style configuration for Livewire text widgets, serializable across the wire.
 *
 * Styling resolves on the host: [token] picks a base style from the host's
 * `MaterialTheme.typography` (or `LocalTextStyle` when null), then every specified
 * field here overrides the base. Unspecified/null fields inherit from the base.
 *
 * Start from a Material3 token via `LivewireTheme.typography` and override with [copy]:
 *
 * ```
 * Text("Hi", style = LivewireTheme.typography.titleMedium.copy(letterSpacing = 1.sp))
 * Text("Hi", style = LivewireTextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold))
 * ```
 *
 * Unlike `androidx.compose.ui.text.TextStyle`, this type only models properties that can
 * cross the wire. Not supported: `fontSynthesis`, `fontFeatureSettings`, `baselineShift`,
 * `textGeometricTransform`, `localeList`, `drawStyle`, `textDirection`, `textIndent`,
 * `platformStyle`, `lineHeightStyle`, `lineBreak`, `hyphens`, `textMotion`, brush-based
 * color, and custom/loaded font families (see [LivewireFontFamily]).
 */
@Immutable
@Serializable
data class LivewireTextStyle(
  val token: TypographyToken? = null,
  @Serializable(with = ColorSerializer::class)
  val color: Color = Color.Unspecified,
  @Serializable(with = TextUnitSerializer::class)
  val fontSize: TextUnit = TextUnit.Unspecified,
  @Serializable(with = FontWeightSerializer::class)
  val fontWeight: FontWeight? = null,
  @Serializable(with = FontStyleSerializer::class)
  val fontStyle: FontStyle? = null,
  val fontFamily: LivewireFontFamily? = null,
  @Serializable(with = TextUnitSerializer::class)
  val letterSpacing: TextUnit = TextUnit.Unspecified,
  @Serializable(with = TextDecorationSerializer::class)
  val textDecoration: TextDecoration? = null,
  @Serializable(with = TextAlignSerializer::class)
  val textAlign: TextAlign? = null,
  @Serializable(with = TextUnitSerializer::class)
  val lineHeight: TextUnit = TextUnit.Unspecified,
  @Serializable(with = ColorSerializer::class)
  val background: Color = Color.Unspecified,
  @Serializable(with = ShadowSerializer::class)
  val shadow: Shadow? = null,
)

/**
 * Materializes the override fields (everything except [LivewireTextStyle.token]) as a
 * Compose [TextStyle], suitable for merging onto the host-resolved base style.
 */
fun LivewireTextStyle.toOverrideTextStyle(): TextStyle = TextStyle(
  color = color,
  fontSize = fontSize,
  fontWeight = fontWeight,
  fontStyle = fontStyle,
  fontFamily = fontFamily?.toComposeUi(),
  letterSpacing = letterSpacing,
  textDecoration = textDecoration,
  textAlign = textAlign ?: TextAlign.Unspecified,
  lineHeight = lineHeight,
  background = background,
  shadow = shadow,
)

/**
 * The font families available to Livewire text. Only the platform generic families can
 * cross the wire — the host has no access to fonts loaded in the client process, so
 * custom/loaded `FontFamily` instances are intentionally not supported.
 */
@Serializable
enum class LivewireFontFamily {
  Default,
  SansSerif,
  Serif,
  Monospace,
  Cursive,
  ;

  fun toComposeUi(): FontFamily = when (this) {
    Default -> FontFamily.Default
    SansSerif -> FontFamily.SansSerif
    Serif -> FontFamily.Serif
    Monospace -> FontFamily.Monospace
    Cursive -> FontFamily.Cursive
  }
}
