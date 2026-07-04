package com.livewire.plugin.network.data

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
  val bodyBytes: ByteArray? = null,
  val contentType: String? = null,
  val contentLength: Long? = null,
  val timestamp: Long,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as NetworkResponse

    if (statusCode != other.statusCode) return false
    if (contentLength != other.contentLength) return false
    if (timestamp != other.timestamp) return false
    if (headers != other.headers) return false
    if (body != other.body) return false
    if (!bodyBytes.contentEquals(other.bodyBytes)) return false
    if (contentType != other.contentType) return false

    return true
  }

  override fun hashCode(): Int {
    var result = statusCode
    result = 31 * result + (contentLength?.hashCode() ?: 0)
    result = 31 * result + timestamp.hashCode()
    result = 31 * result + headers.hashCode()
    result = 31 * result + (body?.hashCode() ?: 0)
    result = 31 * result + (bodyBytes?.contentHashCode() ?: 0)
    result = 31 * result + (contentType?.hashCode() ?: 0)
    return result
  }
}
