package com.r0adkll.livewire.plugin.network.okhttp

import com.r0adkll.livewire.plugin.network.data.NetworkEventCollector
import com.r0adkll.livewire.plugin.network.data.NetworkRequest
import com.r0adkll.livewire.plugin.network.data.NetworkResponse
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

    // Capture response (non-consuming read)
    val responseBody = response.body?.let { body ->
      val source = body.source()
      source.request(maxBodySize)
      source.buffer.clone().readUtf8()
    }

    val networkResponse = NetworkResponse(
      statusCode = response.code,
      headers = response.headers.toMap(),
      body = responseBody,
      contentType = response.body?.contentType()?.toString(),
      contentLength = response.body?.contentLength(),
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
