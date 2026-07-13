package com.livewire.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.livewire.ui.composition.LivewireComposable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

val LocalLivewireTheme = compositionLocalOf {
  LivewireTheme()
}

@Serializable
data class LivewireTheme(
  @Serializable(with = ColorSchemeSerializer::class)
  val lightColorScheme: ColorScheme = defaultLightColorScheme(),
  @Serializable(with = ColorSchemeSerializer::class)
  val darkColorScheme: ColorScheme = defaultDarkColorScheme(),
) {

  companion object {

    val colorScheme: ColorScheme
      @Composable get() = if (currentDarkMode()) {
        LocalLivewireTheme.current.darkColorScheme
      } else {
        LocalLivewireTheme.current.lightColorScheme
      }

    val typography: LivewireTypography get() = LivewireTypography

  }
}

@LivewireComposable
@Composable
fun LivewireTheme(
  theme: LivewireTheme,
  darkMode: Boolean = false,
  content: @LivewireComposable @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalLivewireTheme provides theme,
    LocalDarkMode provides darkMode,
  ) {
    content()
  }
}

object ColorSchemeSerializer : KSerializer<ColorScheme> {

  @Serializable
  private data class ColorSchemeSurrogate(
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val onPrimaryContainer: Int,
    val inversePrimary: Int,
    val secondary: Int,
    val onSecondary: Int,
    val secondaryContainer: Int,
    val onSecondaryContainer: Int,
    val tertiary: Int,
    val onTertiary: Int,
    val tertiaryContainer: Int,
    val onTertiaryContainer: Int,
    val background: Int,
    val onBackground: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val surfaceTint: Int,
    val inverseSurface: Int,
    val inverseOnSurface: Int,
    val error: Int,
    val onError: Int,
    val errorContainer: Int,
    val onErrorContainer: Int,
    val outline: Int,
    val outlineVariant: Int,
    val scrim: Int,
    val surfaceBright: Int,
    val surfaceDim: Int,
    val surfaceContainer: Int,
    val surfaceContainerHigh: Int,
    val surfaceContainerHighest: Int,
    val surfaceContainerLow: Int,
    val surfaceContainerLowest: Int,
    val primaryFixed: Int,
    val primaryFixedDim: Int,
    val onPrimaryFixed: Int,
    val onPrimaryFixedVariant: Int,
    val secondaryFixed: Int,
    val secondaryFixedDim: Int,
    val onSecondaryFixed: Int,
    val onSecondaryFixedVariant: Int,
    val tertiaryFixed: Int,
    val tertiaryFixedDim: Int,
    val onTertiaryFixed: Int,
    val onTertiaryFixedVariant: Int,
  )

  override val descriptor: SerialDescriptor = ColorSchemeSurrogate.serializer().descriptor

  override fun serialize(encoder: Encoder, value: ColorScheme) {
    val surrogate = ColorSchemeSurrogate(
      primary = value.primary.toArgb(),
      onPrimary = value.onPrimary.toArgb(),
      primaryContainer = value.primaryContainer.toArgb(),
      onPrimaryContainer = value.onPrimaryContainer.toArgb(),
      inversePrimary = value.inversePrimary.toArgb(),
      secondary = value.secondary.toArgb(),
      onSecondary = value.onSecondary.toArgb(),
      secondaryContainer = value.secondaryContainer.toArgb(),
      onSecondaryContainer = value.onSecondaryContainer.toArgb(),
      tertiary = value.tertiary.toArgb(),
      onTertiary = value.onTertiary.toArgb(),
      tertiaryContainer = value.tertiaryContainer.toArgb(),
      onTertiaryContainer = value.onTertiaryContainer.toArgb(),
      background = value.background.toArgb(),
      onBackground = value.onBackground.toArgb(),
      surface = value.surface.toArgb(),
      onSurface = value.onSurface.toArgb(),
      surfaceVariant = value.surfaceVariant.toArgb(),
      onSurfaceVariant = value.onSurfaceVariant.toArgb(),
      surfaceTint = value.surfaceTint.toArgb(),
      inverseSurface = value.inverseSurface.toArgb(),
      inverseOnSurface = value.inverseOnSurface.toArgb(),
      error = value.error.toArgb(),
      onError = value.onError.toArgb(),
      errorContainer = value.errorContainer.toArgb(),
      onErrorContainer = value.onErrorContainer.toArgb(),
      outline = value.outline.toArgb(),
      outlineVariant = value.outlineVariant.toArgb(),
      scrim = value.scrim.toArgb(),
      surfaceBright = value.surfaceBright.toArgb(),
      surfaceDim = value.surfaceDim.toArgb(),
      surfaceContainer = value.surfaceContainer.toArgb(),
      surfaceContainerHigh = value.surfaceContainerHigh.toArgb(),
      surfaceContainerHighest = value.surfaceContainerHighest.toArgb(),
      surfaceContainerLow = value.surfaceContainerLow.toArgb(),
      surfaceContainerLowest = value.surfaceContainerLowest.toArgb(),
      primaryFixed = value.primaryFixed.toArgb(),
      primaryFixedDim = value.primaryFixedDim.toArgb(),
      onPrimaryFixed = value.onPrimaryFixed.toArgb(),
      onPrimaryFixedVariant = value.onPrimaryFixedVariant.toArgb(),
      secondaryFixed = value.secondaryFixed.toArgb(),
      secondaryFixedDim = value.secondaryFixedDim.toArgb(),
      onSecondaryFixed = value.onSecondaryFixed.toArgb(),
      onSecondaryFixedVariant = value.onSecondaryFixedVariant.toArgb(),
      tertiaryFixed = value.tertiaryFixed.toArgb(),
      tertiaryFixedDim = value.tertiaryFixedDim.toArgb(),
      onTertiaryFixed = value.onTertiaryFixed.toArgb(),
      onTertiaryFixedVariant = value.onTertiaryFixedVariant.toArgb(),
    )
    encoder.encodeSerializableValue(ColorSchemeSurrogate.serializer(), surrogate)
  }

  override fun deserialize(decoder: Decoder): ColorScheme {
    val surrogate = decoder.decodeSerializableValue(ColorSchemeSurrogate.serializer())
    return ColorScheme(
      primary = Color(surrogate.primary),
      onPrimary = Color(surrogate.onPrimary),
      primaryContainer = Color(surrogate.primaryContainer),
      onPrimaryContainer = Color(surrogate.onPrimaryContainer),
      inversePrimary = Color(surrogate.inversePrimary),
      secondary = Color(surrogate.secondary),
      onSecondary = Color(surrogate.onSecondary),
      secondaryContainer = Color(surrogate.secondaryContainer),
      onSecondaryContainer = Color(surrogate.onSecondaryContainer),
      tertiary = Color(surrogate.tertiary),
      onTertiary = Color(surrogate.onTertiary),
      tertiaryContainer = Color(surrogate.tertiaryContainer),
      onTertiaryContainer = Color(surrogate.onTertiaryContainer),
      background = Color(surrogate.background),
      onBackground = Color(surrogate.onBackground),
      surface = Color(surrogate.surface),
      onSurface = Color(surrogate.onSurface),
      surfaceVariant = Color(surrogate.surfaceVariant),
      onSurfaceVariant = Color(surrogate.onSurfaceVariant),
      surfaceTint = Color(surrogate.surfaceTint),
      inverseSurface = Color(surrogate.inverseSurface),
      inverseOnSurface = Color(surrogate.inverseOnSurface),
      error = Color(surrogate.error),
      onError = Color(surrogate.onError),
      errorContainer = Color(surrogate.errorContainer),
      onErrorContainer = Color(surrogate.onErrorContainer),
      outline = Color(surrogate.outline),
      outlineVariant = Color(surrogate.outlineVariant),
      scrim = Color(surrogate.scrim),
      surfaceBright = Color(surrogate.surfaceBright),
      surfaceDim = Color(surrogate.surfaceDim),
      surfaceContainer = Color(surrogate.surfaceContainer),
      surfaceContainerHigh = Color(surrogate.surfaceContainerHigh),
      surfaceContainerHighest = Color(surrogate.surfaceContainerHighest),
      surfaceContainerLow = Color(surrogate.surfaceContainerLow),
      surfaceContainerLowest = Color(surrogate.surfaceContainerLowest),
      primaryFixed = Color(surrogate.primaryFixed),
      primaryFixedDim = Color(surrogate.primaryFixedDim),
      onPrimaryFixed = Color(surrogate.onPrimaryFixed),
      onPrimaryFixedVariant = Color(surrogate.onPrimaryFixedVariant),
      secondaryFixed = Color(surrogate.secondaryFixed),
      secondaryFixedDim = Color(surrogate.secondaryFixedDim),
      onSecondaryFixed = Color(surrogate.onSecondaryFixed),
      onSecondaryFixedVariant = Color(surrogate.onSecondaryFixedVariant),
      tertiaryFixed = Color(surrogate.tertiaryFixed),
      tertiaryFixedDim = Color(surrogate.tertiaryFixedDim),
      onTertiaryFixed = Color(surrogate.onTertiaryFixed),
      onTertiaryFixedVariant = Color(surrogate.onTertiaryFixedVariant),
    )
  }
}
