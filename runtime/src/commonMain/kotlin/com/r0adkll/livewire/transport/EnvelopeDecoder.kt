package com.r0adkll.livewire.transport

import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.protocol.Payload
import com.r0adkll.livewire.protocol.payloadEnvelopeFromJsonString

class EnvelopeDecoder(
  private val payloadDecoders: Set<PayloadDecoder>,
) {

  suspend fun decode(message: String): Payload? {
    val type = payloadEnvelopeFromJsonString(message).payload.type

    payloadDecoders.forEach { decoder ->
      val payload = with(decoder) { EnvelopeJson.decode(type, message) }
      if (payload != null) return payload
    }

    return null
  }
}