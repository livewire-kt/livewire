package com.livewire.plugin.network.okhttp

import com.livewire.plugin.network.data.NetworkEventCollector
import com.livewire.plugin.network.data.NetworkRequest
import com.livewire.plugin.network.data.NetworkResponse
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class LivewireNetworkInterceptor(
  private val maxBodySize: Long = MAX_BODY_SIZE,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val startTime = System.currentTimeMillis()

    // Capture request
    val requestBody = request.body?.let { body ->
      if (body.contentLength() <= maxBodySize) {
        val buffer = Buffer()
        body.writeTo(buffer)
        buffer.readUtf8()
      } else {
        "(body too large: ${body.contentLength()} bytes)"
      }
    }

    val networkRequest = NetworkRequest(
      method = request.method,
      url = request.url.toString(),
      headers = request.headers.toMap(),
      body = requestBody,
      contentType = request.body?.contentType()?.toString(),
      contentLength = request.body?.contentLength(),
      timestamp = startTime,
    )

    val eventId = NetworkEventCollector.recordRequest(networkRequest)

    // Execute request
    val response: Response
    try {
      response = chain.proceed(request)
    } catch (e: Exception) {
      val durationMs = System.currentTimeMillis() - startTime
      NetworkEventCollector.recordError(eventId, e.message ?: e.toString(), durationMs)
      throw e
    }

    val durationMs = System.currentTimeMillis() - startTime

    val body = response.body
    val contentType = body.contentType()?.toString()
    val isImage = contentType?.startsWith("image/") == true

    val responseBody: String?
    val responseBodyBytes: ByteArray?
    val source = body.source()
    source.request(maxBodySize)
    val snapshot = source.buffer.clone()
    if (isImage) {
      val bytes = snapshot.readByteArray()
      responseBodyBytes = if (bytes.size <= maxBodySize) bytes else null
      responseBody = null
    } else {
      responseBody = snapshot.readUtf8()
      responseBodyBytes = null
    }

    val networkResponse = NetworkResponse(
      statusCode = response.code,
      headers = response.headers.toMap(),
      body = responseBody,
      bodyBytes = responseBodyBytes,
      contentType = contentType,
      contentLength = body.contentLength(),
      timestamp = System.currentTimeMillis(),
    )

    NetworkEventCollector.recordResponse(eventId, networkResponse, durationMs)

    return response
  }

  private fun okhttp3.Headers.toMap(): Map<String, String> {
    return (0 until size).associate { i -> name(i) to value(i) }
  }

  companion object {
    private const val MAX_BODY_SIZE = 256L * 1024 // 256 KB
  }
}
