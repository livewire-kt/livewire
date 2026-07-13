package com.livewire.text

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.livewire.ui.text.LivewireFontFamily
import com.livewire.ui.text.LivewireTextStyle
import com.livewire.ui.text.TypographyToken
import com.livewire.ui.text.toOverrideTextStyle
import com.livewire.ui.theme.LivewireTypography
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LivewireTextStyleTest {

  @Test
  fun toOverrideTextStyleMapsAllFields() {
    val style = LivewireTextStyle(
      color = Color.Red,
      fontSize = 18f.sp,
      fontWeight = FontWeight.SemiBold,
      fontStyle = FontStyle.Italic,
      fontFamily = LivewireFontFamily.Monospace,
      letterSpacing = 1f.sp,
      textDecoration = TextDecoration.Underline,
      textAlign = TextAlign.Center,
      lineHeight = 24f.sp,
      background = Color.Yellow,
      shadow = Shadow(Color.Black, Offset(1f, 2f), blurRadius = 3f),
    )

    val compose = style.toOverrideTextStyle()

    assertEquals(Color.Red, compose.color)
    assertEquals(18f.sp, compose.fontSize)
    assertEquals(FontWeight.SemiBold, compose.fontWeight)
    assertEquals(FontStyle.Italic, compose.fontStyle)
    assertEquals(FontFamily.Monospace, compose.fontFamily)
    assertEquals(1f.sp, compose.letterSpacing)
    assertEquals(TextDecoration.Underline, compose.textDecoration)
    assertEquals(TextAlign.Center, compose.textAlign)
    assertEquals(24f.sp, compose.lineHeight)
    assertEquals(Color.Yellow, compose.background)
    assertEquals(Shadow(Color.Black, Offset(1f, 2f), blurRadius = 3f), compose.shadow)
  }

  @Test
  fun toOverrideTextStyleKeepsUnspecifiedFieldsUnspecified() {
    val compose = LivewireTextStyle(token = TypographyToken.BodyLarge).toOverrideTextStyle()

    assertEquals(Color.Unspecified, compose.color)
    assertEquals(TextUnit.Unspecified, compose.fontSize)
    assertNull(compose.fontWeight)
    assertNull(compose.fontStyle)
    assertNull(compose.fontFamily)
    assertEquals(TextUnit.Unspecified, compose.letterSpacing)
    assertNull(compose.textDecoration)
    assertEquals(TextAlign.Unspecified, compose.textAlign)
    assertEquals(TextUnit.Unspecified, compose.lineHeight)
    assertEquals(Color.Unspecified, compose.background)
    assertNull(compose.shadow)
  }

  @Test
  fun typographyTokensCarryOnlyTheirToken() {
    assertEquals(LivewireTextStyle(token = TypographyToken.TitleMedium), LivewireTypography.titleMedium)
    assertEquals(TypographyToken.LabelSmall, LivewireTypography.labelSmall.token)
  }

  @Test
  fun copyOnTokenStylePreservesToken() {
    val style = LivewireTypography.titleMedium.copy(letterSpacing = 1f.sp)

    assertEquals(TypographyToken.TitleMedium, style.token)
    assertEquals(1f.sp, style.letterSpacing)
  }
}
