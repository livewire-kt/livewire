package com.r0adkll.livewire.transport

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

interface PayloadDecoder<T : Any> {
  val serializersModule: SerializersModule? get() = null

  suspend fun Json.decodePayload(rawPayload: String): T?
}

val DefaultDecoders: Set<PayloadDecoder<*>>
  get() = setOf()
