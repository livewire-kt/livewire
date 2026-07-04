package com.livewire.protocol

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val DefaultPayloadModule = SerializersModule {
}

val EnvelopeJson = Json {
  classDiscriminator = "type"
  ignoreUnknownKeys = true
  serializersModule = DefaultPayloadModule
}
