package com.r0adkll.livewire.protocol

import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed class Boundary() {

  @Serializable
  data class Start(
    val eventFqName: String,
  ) : Boundary()

  @Serializable
  data class End(
    val eventFqName: String,
  ) : Boundary()

  companion object : PayloadDecoder<Boundary> {
    override suspend fun Json.decodePayload(rawPayload: String): Boundary {
      return decodeFromString(serializer(), rawPayload)
    }

  }
}
