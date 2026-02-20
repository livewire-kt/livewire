package com.r0adkll.livewire.protocol

import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SimpleMessage(
  val message: String,
) : Payload("simple") {

  override fun toString(): String {
    return "Simple($message)"
  }

  // TODO: We should probably generate this?
  companion object : PayloadDecoder {
    override suspend fun Json.decode(
      type: String,
      rawPayload: String
    ): Payload? = when (type) {
      "simple" -> decodeFromEnvelope<SimpleMessage>(rawPayload)
      else -> null
    }
  }
}


