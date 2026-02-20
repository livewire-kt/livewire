package com.r0adkll.livewire.transport

import com.r0adkll.livewire.protocol.Payload
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

interface PayloadDecoder {

  @Throws(SerializationException::class)
  suspend fun Json.decode(type: String, rawPayload: String): Payload?
}