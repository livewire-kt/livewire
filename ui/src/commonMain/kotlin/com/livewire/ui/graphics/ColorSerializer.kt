package com.livewire.ui.graphics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: Color) {
    encoder.encodeInt(value.toArgb())
  }

  override fun deserialize(decoder: Decoder): Color {
    return Color(decoder.decodeInt())
  }
}
