package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.annotations.LivewireLayoutSerializer
import com.r0adkll.livewire.annotations.LivewireModifierSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.protobuf.ProtoBuf

internal val LivewireUiJson = Json {
  classDiscriminator = "__type"
  serializersModule =
    LayoutNodeSerializers().serializersModule +
    LivewireModifierSerializers().serializersModule
}

@OptIn(ExperimentalSerializationApi::class)
internal val LivewireUiProtobuf = ProtoBuf {
  serializersModule =
    LayoutNodeSerializers().serializersModule +
    LivewireModifierSerializers().serializersModule
}

/**
 * This will generate an actual implementation under the hood that contains
 * the built polymorphic serializer mapping to use in the above Json
 * implementation
 */
@LivewireLayoutSerializer
expect class LayoutNodeSerializers() {
  val serializersModule: SerializersModule
}

/**
 * This will generate an actual implementation under the hood that contains
 * the built polymorphic serializer mapping to use in the above Json
 * implementation
 */
@LivewireModifierSerializer
expect class LivewireModifierSerializers() {
  val serializersModule: SerializersModule
}
