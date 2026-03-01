package com.r0adkll.livewire.plugin.network.data

data class NetworkEvent(
  val id: String,
  val request: NetworkRequest,
  val response: NetworkResponse? = null,
  val durationMs: Long? = null,
  val error: String? = null,
)

data class NetworkRequest(
  val method: String,
  val url: String,
  val headers: Map<String, String>,
  val body: String? = null,
  val contentType: String? = null,
  val contentLength: Long? = null,
  val timestamp: Long,
)

data class NetworkResponse(
  val statusCode: Int,
  val headers: Map<String, String>,
  val body: String? = null,
  val contentType: String? = null,
  val contentLength: Long? = null,
  val timestamp: Long,
)
