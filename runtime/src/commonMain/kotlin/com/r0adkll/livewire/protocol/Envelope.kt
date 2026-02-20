package com.r0adkll.livewire.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule

val DefaultPayloadModule = SerializersModule {

}

val EnvelopeJson = Json {
  ignoreUnknownKeys = true
  serializersModule = DefaultPayloadModule
}

/**
 * Typed message envelope for WebSocket communication.
 */
@Serializable
data class Envelope<T>(
    val payload: T,
)

/** Wire-level envelope whose payload is an opaque [JsonElement]. */
typealias PayloadEnvelope = Envelope<Payload>

private val PayloadSerializer = Envelope.serializer(Payload.serializer())

inline fun <reified T> Envelope<T>.toJsonString(json: Json = EnvelopeJson): String =
  json.encodeToString(this)

fun payloadEnvelopeFromJsonString(
  text: String,
  json: Json = EnvelopeJson,
): PayloadEnvelope = json.decodeFromString(PayloadSerializer, text)

inline fun <reified T : Payload> Json.decodeFromEnvelope(
  text: String,
): T {
  val envelope = decodeFromString<Envelope<T>>(text)
  return envelope.payload
}