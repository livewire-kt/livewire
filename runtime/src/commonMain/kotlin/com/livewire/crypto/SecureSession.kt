package com.livewire.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.operations.IvAuthenticatedCipher
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// Crypto operations are suspend (not *Blocking) throughout: the WebCrypto provider used on
// web targets rejects blocking calls.
@OptIn(DelicateCryptographyApi::class, ExperimentalAtomicApi::class)
class SecureSession private constructor(
  private val sendCipher: IvAuthenticatedCipher,
  private val receiveCipher: IvAuthenticatedCipher,
  private val sendNoncePrefix: ByteArray,
  private val receiveNoncePrefix: ByteArray,
) {
  private val sendCounter = AtomicLong(0L)
  private val lastReceivedCounter = AtomicLong(-1L)

  suspend fun encryptText(plaintext: ByteArray): ByteArray = encryptFrame(TagText, plaintext)

  suspend fun encryptBinary(plaintext: ByteArray): ByteArray = encryptFrame(TagBinary, plaintext)

  suspend fun decrypt(encrypted: ByteArray): Pair<Byte, ByteArray> {
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

    val plaintext = receiveCipher.decryptWithIv(nonce, ciphertext, null)

    val counter = readCounter(nonce)
    val previous = lastReceivedCounter.load()
    if (counter <= previous) {
      error("Replayed or out-of-order frame: counter=$counter last=$previous")
    }
    lastReceivedCounter.store(counter)

    return frameTypeTag to plaintext
  }

  private suspend fun encryptFrame(frameTypeTag: Byte, plaintext: ByteArray): ByteArray {
    val nonce = buildNonce(sendNoncePrefix, sendCounter.fetchAndAdd(1))
    val ciphertext = sendCipher.encryptWithIv(nonce, plaintext, null)

    val encryptedFrame = ByteArray(HeaderSize + ciphertext.size)
    encryptedFrame[0] = frameTypeTag
    nonce.copyInto(encryptedFrame, 1)
    ciphertext.copyInto(encryptedFrame, HeaderSize)

    return encryptedFrame
  }

  private fun readCounter(nonce: ByteArray): Long {
    var counter = 0L
    for (byteIndex in 0..7) {
      counter = (counter shl 8) or (nonce[NoncePrefixSize + byteIndex].toLong() and 0xFF)
    }
    return counter
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

    suspend fun create(
      sendKey: ByteArray,
      receiveKey: ByteArray,
      sendNoncePrefix: ByteArray,
      receiveNoncePrefix: ByteArray,
    ): SecureSession {
      val aesGcm = CryptographyProvider.Default.get(AES.GCM)
      return SecureSession(
        sendCipher = aesGcm
          .keyDecoder()
          .decodeFromByteArray(AES.Key.Format.RAW, sendKey)
          .cipher(),
        receiveCipher = aesGcm
          .keyDecoder()
          .decodeFromByteArray(AES.Key.Format.RAW, receiveKey)
          .cipher(),
        sendNoncePrefix = sendNoncePrefix,
        receiveNoncePrefix = receiveNoncePrefix,
      )
    }
  }
}

private const val NoncePrefixSize = 4
private const val NonceSize = 12
private const val HeaderSize = 1 + NonceSize
