package com.livewire.plugin.network.ktor

import com.livewire.plugin.network.data.NetworkEventCollector
import com.livewire.plugin.network.data.NetworkRequest
import com.livewire.plugin.network.data.NetworkResponse
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.util.AttributeKey

class LivewireNetworkPluginConfig {
  var maxBodySize: Long = 256L * 1024 // 256 KB
}

private val EventIdKey = AttributeKey<String>("LivewireEventId")
private val StartTimeKey = AttributeKey<Long>("LivewireStartTime")

val LivewireNetworkPlugin = createClientPlugin(
  "LivewireNetworkPlugin",
  ::LivewireNetworkPluginConfig,
) {
  val maxBodySize = pluginConfig.maxBodySize

  onRequest { request, _ ->
    val startTime = currentTimeMillis()
    request.attributes.put(StartTimeKey, startTime)

    val headers = mutableMapOf<String, String>()
    request.headers.entries().forEach { (key, values) ->
      headers[key] = values.joinToString(", ")
    }

    val networkRequest = NetworkRequest(
      method = request.method.value,
      url = request.url.buildString(),
      headers = headers,
      body = null,
      contentType = request.contentType()?.toString(),
      contentLength = request.headers[HttpHeaders.ContentLength]?.toLongOrNull(),
      timestamp = startTime,
    )

    val eventId = NetworkEventCollector.recordRequest(networkRequest)
    request.attributes.put(EventIdKey, eventId)
  }

  // Runs after content negotiation, so serialized bodies are available.
  // Streaming bodies (channel content) are skipped — reading them here
  // would consume the request.
  on(SendingRequest) { request, content ->
    val eventId = request.attributes.getOrNull(EventIdKey) ?: return@on
    if (content is OutgoingContent.ByteArrayContent) {
      val body = try {
        val text = content.bytes().decodeToString()
        if (text.length <= maxBodySize) text else text.take(maxBodySize.toInt())
      } catch (_: Exception) {
        null
      }
      if (body != null) {
        NetworkEventCollector.updateRequestBody(
          id = eventId,
          body = body,
          contentType = content.contentType?.toString(),
          contentLength = content.contentLength,
        )
      }
    }
  }

  val observer = ResponseObserver.prepare {
    onResponse { response ->
      val eventId = response.call.request.attributes.getOrNull(EventIdKey) ?: return@onResponse
      val startTime = response.call.request.attributes.getOrNull(StartTimeKey) ?: return@onResponse
      val durationMs = currentTimeMillis() - startTime

      val contentType = response.contentType()?.toString()
      // TODO: probably need better heuristics than this.
      // TODO: can handle more types this way too. videos? gifs?
      val isImage = contentType?.startsWith("image/") == true

      var responseBody: String? = null
      var responseBodyBytes: ByteArray? = null
      if (isImage) {
        responseBodyBytes = try {
          val bytes = response.readRawBytes()
          if (bytes.size <= maxBodySize) bytes else null
        } catch (_: Exception) {
          null
        }
      } else {
        responseBody = try {
          val text = response.bodyAsText()
          if (text.length <= maxBodySize) text else text.take(maxBodySize.toInt())
        } catch (_: Exception) {
          null
        }
      }

      val responseHeaders = mutableMapOf<String, String>()
      response.headers.entries().forEach { (key, values) ->
        responseHeaders[key] = values.joinToString(", ")
      }

      val networkResponse = NetworkResponse(
        statusCode = response.status.value,
        headers = responseHeaders,
        body = responseBody,
        bodyBytes = responseBodyBytes,
        contentType = contentType,
        contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull(),
        timestamp = currentTimeMillis(),
      )

      NetworkEventCollector.recordResponse(eventId, networkResponse, durationMs)
    }
  }
  ResponseObserver.install(observer, client)
}
