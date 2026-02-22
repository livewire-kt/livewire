package com.r0adkll.livewire.transport

import com.r0adkll.livewire.protocol.EnvelopeJson
import kotlinx.serialization.json.Json

class EnvelopeDecoder(
  private val payloadDecoders: Set<PayloadDecoder<*>>,
) {

  suspend fun decode(message: String): Any? {
    return payloadDecoders.firstNotNullOfOrNull { decoder ->
      val json = decoder.serializersModule?.let {
        Json(EnvelopeJson) {
          serializersModule = it
        }
      } ?: EnvelopeJson

      try {
        with(decoder) { json.decodePayload(message) }
      } catch (_: Exception) {
        null
      }
    }
  }
}
