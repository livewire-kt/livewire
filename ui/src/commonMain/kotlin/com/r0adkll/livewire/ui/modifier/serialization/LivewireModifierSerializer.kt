package com.r0adkll.livewire.ui.modifier.serialization

import com.r0adkll.livewire.ui.modifier.BackgroundModifier
import com.r0adkll.livewire.ui.modifier.CombinedLivewireModifier
import com.r0adkll.livewire.ui.modifier.HeightModifier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.PaddingModifier
import com.r0adkll.livewire.ui.modifier.WidthModifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@OptIn(ExperimentalSerializationApi::class)
object LivewireModifierSerializer : KSerializer<LivewireModifier> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.r0adkll.livewire.ui.LivewireModifier", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LivewireModifier) {
    val byteString = LivewireModifierCbor.encodeToHexString(value)
    encoder.encodeString(byteString)
  }

  override fun deserialize(decoder: Decoder): LivewireModifier {
    val byteString = decoder.decodeString()
    return LivewireModifierCbor.decodeFromHexString(byteString)
  }
}

@OptIn(ExperimentalSerializationApi::class)
private val LivewireModifierCbor = Cbor {
  serializersModule = SerializersModule {
    polymorphic(LivewireModifier::class) {
      subclass(WidthModifier::class, WidthModifier.serializer())
      subclass(HeightModifier::class, HeightModifier.serializer())
      subclass(PaddingModifier::class, PaddingModifier.serializer())
      subclass(BackgroundModifier::class, BackgroundModifier.serializer())
      subclass(CombinedLivewireModifier::class, CombinedLivewireModifier.serializer())
    }
  }
}
