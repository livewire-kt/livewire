package com.r0adkll.livewire.protocol

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val DefaultPayloadModule = SerializersModule {
}

val EnvelopeJson = Json {
  ignoreUnknownKeys = true
  serializersModule = DefaultPayloadModule
}
