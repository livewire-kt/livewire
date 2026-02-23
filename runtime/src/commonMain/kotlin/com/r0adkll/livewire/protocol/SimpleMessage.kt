package com.r0adkll.livewire.protocol

import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SimpleMessage(
  val message: String,
) {

  override fun toString(): String {
    return "Simple($message)"
  }

  // TODO: We should probably generate this?
  companion object : PayloadDecoder<SimpleMessage> {
    override suspend fun Json.decodePayload(
      rawPayload: String
    ): SimpleMessage = decodeFromString<SimpleMessage>(rawPayload)
  }
}


