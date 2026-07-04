package com.livewire.crypto

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256

class LivewireHandshake {
  suspend fun perform(
    sendBytes: suspend (ByteArray) -> Unit,
    receiveBytes: suspend () -> ByteArray,
  ): SecureSession {
    val provider = CryptographyProvider.Default
    val ecdh = provider.get(ECDH)
    val curve = EC.Curve.P256
    val keyPair = ecdh.keyPairGenerator(curve).generateKey()
    val publicKeyBytes = keyPair.publicKey.encodeToByteArray(EC.PublicKey.Format.RAW.Uncompressed)

    sendBytes(HandshakeMessage(publicKeyBytes).encoded())

    val peerMessage = HandshakeMessage.decode(receiveBytes())

    val peerPublicKey = ecdh
      .publicKeyDecoder(curve)
      .decodeFromByteArray(EC.PublicKey.Format.RAW.Uncompressed, peerMessage.publicKey)
    val sharedSecret = keyPair
      .privateKey
      .sharedSecretGenerator()
      .generateSharedSecretToByteArray(peerPublicKey)

    val salt = generateSalt(publicKeyBytes, peerMessage.publicKey)

    val derived = provider
      .get(HKDF)
      .secretDerivation(
        digest = SHA256,
        outputSize = 72.bytes,
        salt = salt,
        info = "livewire-encryption-v1".encodeToByteArray(),
      )
      .deriveSecretToByteArray(sharedSecret)

    val key1 = derived.copyOfRange(0, 32)
    val key2 = derived.copyOfRange(32, 64)
    val nonce1 = derived.copyOfRange(64, 68)
    val nonce2 = derived.copyOfRange(68, 72)

    val isSender = publicKeyBytes.compareTo(peerMessage.publicKey) < 0
    return if (isSender) {
      SecureSession(sendKey = key1, receiveKey = key2, sendNoncePrefix = nonce1, receiveNoncePrefix = nonce2)
    } else {
      SecureSession(sendKey = key2, receiveKey = key1, sendNoncePrefix = nonce2, receiveNoncePrefix = nonce1)
    }
  }

  private fun generateSalt(a: ByteArray, b: ByteArray): ByteArray {
    return if (a.compareTo(b) <= 0) a + b else b + a
  }

  private fun ByteArray.compareTo(other: ByteArray): Int {
    val minSize = minOf(size, other.size)
    for (i in 0 until minSize) {
      val diff = (this[i].toInt() and 0xFF) - (other[i].toInt() and 0xFF)
      if (diff != 0) return diff
    }
    return size - other.size
  }
}
