package com.r0adkll.livewire.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES

@OptIn(DelicateCryptographyApi::class)
class SecureSession(
  sendKey: ByteArray,
  receiveKey: ByteArray,
  private val sendNoncePrefix: ByteArray,
  private val receiveNoncePrefix: ByteArray,
) {
  private var sendCounter: Long = 0
  private val aesGcm = CryptographyProvider.Default.get(AES.GCM)
  private val sendCipher = aesGcm
    .keyDecoder()
    .decodeFromByteArrayBlocking(AES.Key.Format.RAW, sendKey)
    .cipher()
  private val receiveCipher = aesGcm
    .keyDecoder()
    .decodeFromByteArrayBlocking(AES.Key.Format.RAW, receiveKey)
    .cipher()

  fun encryptText(plaintext: ByteArray): ByteArray = encryptFrame(TagText, plaintext)

  fun encryptBinary(plaintext: ByteArray): ByteArray = encryptFrame(TagBinary, plaintext)

  fun decrypt(encrypted: ByteArray): Pair<Byte, ByteArray> {
    require(encrypted.size >= HeaderSize) { "Encrypted frame too short" }

    val frameTypeTag = encrypted[0]
    require(frameTypeTag == TagText || frameTypeTag == TagBinary) { "Unknown frame tag: $frameTypeTag" }

    val nonce = encrypted.copyOfRange(1, 1 + NonceSize)
    val ciphertext = encrypted.copyOfRange(HeaderSize, encrypted.size)

    for (byteIndex in 0 until NoncePrefixSize) {
      if (nonce[byteIndex] != receiveNoncePrefix[byteIndex]) {
        error("Nonce prefix mismatch")
      }
    }

    return frameTypeTag to receiveCipher.decryptWithIvBlocking(nonce, ciphertext, null)
  }

  private fun encryptFrame(frameTypeTag: Byte, plaintext: ByteArray): ByteArray {
    val nonce = buildNonce(sendNoncePrefix, sendCounter++)
    val ciphertext = sendCipher.encryptWithIvBlocking(nonce, plaintext, null)

    val encryptedFrame = ByteArray(HeaderSize + ciphertext.size)
    encryptedFrame[0] = frameTypeTag
    nonce.copyInto(encryptedFrame, 1)
    ciphertext.copyInto(encryptedFrame, HeaderSize)

    return encryptedFrame
  }

  internal fun buildNonce(prefix: ByteArray, counter: Long): ByteArray {
    val nonce = ByteArray(NonceSize)
    prefix.copyInto(nonce, 0, 0, NoncePrefixSize)
    for (byteIndex in 0..7) {
      nonce[NoncePrefixSize + byteIndex] = (counter ushr (56 - byteIndex * 8) and 0xFF).toByte()
    }
    return nonce
  }

  companion object {
    const val TagText: Byte = 0x01
    const val TagBinary: Byte = 0x02
  }
}

private const val NoncePrefixSize = 4
private const val NonceSize = 12
private const val HeaderSize = 1 + NonceSize
