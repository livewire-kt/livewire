package com.r0adkll.livewire.plugin.chat

import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent
import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class ChatHostEvent : HostEvent {

  @Serializable
  data class Message(val message: String) : ChatHostEvent()

  // TODO: Either code-gen or automate this
  companion object : PayloadDecoder<ChatHostEvent> {
    override suspend fun Json.decodePayload(rawPayload: String): ChatHostEvent? {
      return decodeFromString(rawPayload)
    }

  }
}

@Serializable
sealed class ChatClientEvent : ClientEvent {

  @Serializable
  data class Message(val msg: String) : ChatClientEvent()

  companion object : PayloadDecoder<ChatClientEvent> {
    override suspend fun Json.decodePayload(rawPayload: String): ChatClientEvent? {
      return decodeFromString(rawPayload)
    }

  }
}

