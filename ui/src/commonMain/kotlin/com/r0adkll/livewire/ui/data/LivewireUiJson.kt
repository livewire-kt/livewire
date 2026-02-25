package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.annotations.LivewireLayoutSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val LivewireUiJson = Json {
  classDiscriminator = "type"
  serializersModule = LayoutNodeSerializers().serializersModule
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
