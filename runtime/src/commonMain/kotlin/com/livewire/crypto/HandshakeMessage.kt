package com.livewire.crypto

class HandshakeMessage(val publicKey: ByteArray) {
  fun encoded(): ByteArray {
    val encoded = ByteArray(2 + publicKey.size)
    encoded[0] = (publicKey.size shr 8 and 0xFF).toByte()
    encoded[1] = (publicKey.size and 0xFF).toByte()
    publicKey.copyInto(encoded, 2)
    return encoded
  }

  companion object {
    fun decode(bytes: ByteArray): HandshakeMessage {
      require(bytes.size >= 2) { "Handshake message too short" }

      val keyLength = (bytes[0].toInt() and 0xFF shl 8) or (bytes[1].toInt() and 0xFF)
      require(bytes.size == 2 + keyLength) { "Invalid key exchange message length" }
      return HandshakeMessage(bytes.copyOfRange(2, 2 + keyLength))
    }
  }
}
