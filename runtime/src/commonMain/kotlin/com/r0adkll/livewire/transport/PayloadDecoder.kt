package com.r0adkll.livewire.transport

import com.r0adkll.livewire.protocol.SimpleMessage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

interface PayloadDecoder<T : Any> {
  val serializersModule: SerializersModule? get() = null

  @Throws(SerializationException::class)
  suspend fun Json.decodePayload(rawPayload: String): T?
}

val DefaultDecoders: Set<PayloadDecoder<*>> get() = setOf(
  SimpleMessage,
)
