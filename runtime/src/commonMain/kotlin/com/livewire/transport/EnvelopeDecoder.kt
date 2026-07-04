package com.livewire.transport

import com.livewire.protocol.EnvelopeJson
import kotlinx.serialization.json.Json

class EnvelopeDecoder(
  payloadDecoders: Set<PayloadDecoder<*>>,
) {
  private val decoderJsons: Map<PayloadDecoder<*>, Json> = payloadDecoders.associateWith { decoder ->
    decoder.serializersModule?.let { Json(EnvelopeJson) { serializersModule = it } } ?: EnvelopeJson
  }

  private val decoders: List<PayloadDecoder<*>> = payloadDecoders.toList()

  fun decode(message: String): Any? {
    val element = try {
      EnvelopeJson.parseToJsonElement(message)
    } catch (_: Exception) {
      return null
    }

    return decoders.firstNotNullOfOrNull { decoder ->
      try {
        with(decoder) { decoderJsons.getValue(decoder).decodePayload(element) }
      } catch (_: Exception) {
        null
      }
    }
  }
}
