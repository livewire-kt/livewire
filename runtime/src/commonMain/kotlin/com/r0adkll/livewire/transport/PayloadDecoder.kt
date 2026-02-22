package com.r0adkll.livewire.transport

import com.r0adkll.livewire.protocol.Boundary
import com.r0adkll.livewire.protocol.SimpleMessage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

interface PayloadDecoder<T : Any> {
  val serializersModule: SerializersModule? get() = null

  fun serializer(): KSerializer<T>

  @Throws(SerializationException::class)
  suspend fun Json.decodePayload(rawPayload: String): T?
}

val DefaultDecoders: Set<PayloadDecoder<*>> get() = setOf(
  SimpleMessage,
  Boundary,
)
