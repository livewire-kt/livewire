package com.livewire.ui.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TextUnitSerializer : KSerializer<TextUnit> {

  @Serializable
  private data class TextUnitSurrogate(
    val value: Float,
    val unit: Type,
  )

  @Serializable
  enum class Type {
    Unspecified,
    Sp,
    Em,
  }

  override val descriptor: SerialDescriptor = TextUnitSurrogate.serializer().descriptor

  override fun serialize(encoder: Encoder, value: TextUnit) {
    val surrogate = when {
      value.isSp -> TextUnitSurrogate(value.value, Type.Sp)
      value.isEm -> TextUnitSurrogate(value.value, Type.Em)
      else -> TextUnitSurrogate(0f, Type.Unspecified)
    }
    encoder.encodeSerializableValue(TextUnitSurrogate.serializer(), surrogate)
  }

  override fun deserialize(decoder: Decoder): TextUnit {
    val surrogate = decoder.decodeSerializableValue(TextUnitSurrogate.serializer())
    return when (surrogate.unit) {
      Type.Sp -> surrogate.value.sp
      Type.Em -> surrogate.value.em
      Type.Unspecified -> TextUnit.Unspecified
    }
  }
}

object FontWeightSerializer : KSerializer<FontWeight> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("FontWeight", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: FontWeight) {
    encoder.encodeInt(value.weight)
  }

  override fun deserialize(decoder: Decoder): FontWeight {
    return FontWeight(decoder.decodeInt())
  }
}

object FontStyleSerializer : KSerializer<FontStyle> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("FontStyle", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: FontStyle) {
    encoder.encodeInt(value.value)
  }

  override fun deserialize(decoder: Decoder): FontStyle {
    return FontStyle(decoder.decodeInt())
  }
}

object TextAlignSerializer : KSerializer<TextAlign> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("TextAlign", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: TextAlign) {
    encoder.encodeInt(value.value)
  }

  override fun deserialize(decoder: Decoder): TextAlign {
    val value = decoder.decodeInt()
    return TextAlign.values().firstOrNull { it.value == value } ?: TextAlign.Unspecified
  }
}

object TextDecorationSerializer : KSerializer<TextDecoration> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("TextDecoration", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: TextDecoration) {
    encoder.encodeInt(value.mask)
  }

  override fun deserialize(decoder: Decoder): TextDecoration {
    val mask = decoder.decodeInt()
    val decorations = buildList {
      if (mask and TextDecoration.Underline.mask != 0) add(TextDecoration.Underline)
      if (mask and TextDecoration.LineThrough.mask != 0) add(TextDecoration.LineThrough)
    }
    return if (decorations.isEmpty()) TextDecoration.None else TextDecoration.combine(decorations)
  }
}

object ShadowSerializer : KSerializer<Shadow> {

  @Serializable
  private data class ShadowSurrogate(
    val color: Int,
    val offsetX: Float,
    val offsetY: Float,
    val blurRadius: Float,
  )

  override val descriptor: SerialDescriptor = ShadowSurrogate.serializer().descriptor

  override fun serialize(encoder: Encoder, value: Shadow) {
    val surrogate = ShadowSurrogate(
      color = value.color.toArgb(),
      offsetX = value.offset.x,
      offsetY = value.offset.y,
      blurRadius = value.blurRadius,
    )
    encoder.encodeSerializableValue(ShadowSurrogate.serializer(), surrogate)
  }

  override fun deserialize(decoder: Decoder): Shadow {
    val surrogate = decoder.decodeSerializableValue(ShadowSurrogate.serializer())
    return Shadow(
      color = Color(surrogate.color),
      offset = Offset(surrogate.offsetX, surrogate.offsetY),
      blurRadius = surrogate.blurRadius,
    )
  }
}
