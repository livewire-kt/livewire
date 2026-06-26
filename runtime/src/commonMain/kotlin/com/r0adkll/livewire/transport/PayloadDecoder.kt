package com.r0adkll.livewire.transport

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule

interface PayloadDecoder<T : Any> {
  val serializersModule: SerializersModule? get() = null
  suspend fun Json.decodePayload(element: JsonElement): T?
}

val DefaultDecoders: Set<PayloadDecoder<*>>
  get() = setOf()
