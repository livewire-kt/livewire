@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

internal data class ParameterInfo(
  val name: String,
  val value: ParameterValue,
)

internal sealed interface ParameterValue {
  val displayValue: String

  data class Text(override val displayValue: String) : ParameterValue

  data class ColorValue(private val value: ULong) : ParameterValue {
    val color = Color(value)

    override val displayValue = when {
      value == Color.Unspecified.value -> "Color.Unspecified"
      color.alpha == 0f && color.red == 0f && color.green == 0f && color.blue == 0f -> "Color.Transparent"
      color.alpha == 1f -> "#${color.red.toHex()}${color.green.toHex()}${color.blue.toHex()}"
      else -> "#${color.alpha.toHex()}${color.red.toHex()}${color.green.toHex()}${color.blue.toHex()}"
    }

    private fun Float.toHex(): String {
      return (255 * this).toInt().toString(16).padStart(2, '0').uppercase()
    }
  }

  data class DpValue(private val value: Float) : ParameterValue {
    override val displayValue: String = when (value) {
      Dp.Hairline.value -> "Dp.Hairline"
      Dp.Infinity.value -> "Dp.Infinity"
      Dp.Unspecified.value -> "Dp.Unspecified"
      else -> "${value}.dp"
    }
  }

  data class TextUnitValue(val value: Long) : ParameterValue {
    private val textUnit = TextUnit(value)

    override val displayValue: String = when {
      textUnit == TextUnit.Unspecified -> "TextUnit.Unspecified"
      textUnit.isSp -> "${textUnit.value}.sp"
      textUnit.isEm ->"${textUnit.value}.em"
      else -> textUnit.toString()
    }
  }

  data class FontStyleValue(val value: Int) : ParameterValue {
    override val displayValue: String = when (value) {
      FontStyle.Normal.value -> "FontStyle.Normal"
      FontStyle.Italic.value -> "FontStyle.Italic"
      else -> "FontStyle($value)"
    }
  }

  data class TextAlignValue(val value: Int) : ParameterValue {
    override val displayValue: String = when (value) {
      TextAlign.Unspecified.value -> "TextAlign.Unspecified"
      TextAlign.Left.value -> "TextAlign.Left"
      TextAlign.Right.value -> "TextAlign.Right"
      TextAlign.Center.value -> "TextAlign.Center"
      TextAlign.Justify.value -> "TextAlign.Justify"
      TextAlign.Start.value -> "TextAlign.Start"
      TextAlign.End.value -> "TextAlign.End"
      else -> "TextAlign($value)"
    }
  }

  data class TextOverflowValue(val value: Int) : ParameterValue {
    override val displayValue: String = when (value) {
      TextOverflow.Clip.value -> "TextOverflow.Clip"
      TextOverflow.Ellipsis.value -> "TextOverflow.Ellipsis"
      TextOverflow.Visible.value -> "TextOverflow.Visible"
      else -> "TextOverflow($value)"
    }
  }

  data class FontWeightValue(val value: Int) : ParameterValue {
    override val displayValue: String = when (value) {
      FontWeight.Thin.weight -> "FontWeight.Thin"
      FontWeight.ExtraLight.weight -> "FontWeight.ExtraLight"
      FontWeight.Light.weight -> "FontWeight.Light"
      FontWeight.Normal.weight -> "FontWeight.Normal"
      FontWeight.Medium.weight -> "FontWeight.Medium"
      FontWeight.SemiBold.weight -> "FontWeight.SemiBold"
      FontWeight.Bold.weight -> "FontWeight.Bold"
      FontWeight.ExtraBold.weight -> "FontWeight.ExtraBold"
      FontWeight.Black.weight -> "FontWeight.Black"
      else -> "FontWeight($value)"
    }
  }

  data object Lambda : ParameterValue {
    override val displayValue: String = "λ"
  }

  companion object {
    fun fromValue(value: Any?, inlineClass: String?): ParameterValue {
      if (value == null) return Text("null")

      if (inlineClass != null) {
        when (inlineClass) {
          Color::class.qualifiedName if value is Long -> return ColorValue(value.toULong())
          TextUnit::class.qualifiedName if value is Long -> return TextUnitValue(value)
          Dp::class.qualifiedName if value is Float -> return DpValue(value)
          FontStyle::class.qualifiedName if value is Int -> return FontStyleValue(value)
          TextAlign::class.qualifiedName if value is Int -> return TextAlignValue(value)
          TextOverflow::class.qualifiedName if value is Int -> return TextOverflowValue(value)
          FontWeight::class.qualifiedName if value is Int -> return FontWeightValue(value)
        }
      }

      return when (value) {
        is String -> Text("\"$value\"")
        is Char -> Text("'$value'")
        is Boolean, is Int, is Long, is Float, is Double -> Text(value.toString())
        is Collection<*> -> Text("${value::class.simpleName ?: "Collection"}(size=${value.size})")
        is Map<*, *> -> Text("${value::class.simpleName ?: "Map"}(size=${value.size})")
        is Enum<*> -> Text(value.name)
        is Function<*> -> Lambda
        else -> {
          val className = value::class.simpleName ?: "Object"
          val toString = value.toString()
          Text(
            displayValue = if (toString.startsWith(className) || toString.length <= 60) {
              toString
            } else {
              "$className@${value.hashCode().toUInt().toString(16)}"
            }
          )
        }
      }
    }
  }
}
