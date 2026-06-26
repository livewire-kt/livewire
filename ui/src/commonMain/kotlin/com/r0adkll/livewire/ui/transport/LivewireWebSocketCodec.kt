package com.r0adkll.livewire.ui.transport

import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import com.r0adkll.livewire.crypto.SecureSession
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.layout.LayoutNode
import io.ktor.websocket.*

class LivewireWebSocketCodec(
  decoders: Collection<PayloadDecoder<*>>,
  var serializationStrategy: LayoutNodeSerializationStrategy = LayoutNodeSerializationStrategy.Default,
  private val outgoingSizeReporter: (Long) -> Unit = {},
) {
  private val envelopeDecoder = EnvelopeDecoder(payloadDecoders = decoders.toSet())

  var secureSession: SecureSession? = null

  fun decryptFrame(frame: Frame): Frame? {
    if (secureSession == null) {
      return when (frame) {
        is Frame.Text, is Frame.Binary -> frame
        else -> null
      }
    }

    if (frame !is Frame.Binary) return null
    val (tag, plaintext) = secureSession!!.decrypt(frame.readBytes())
    return when (tag) {
      SecureSession.TagText -> Frame.Text(true, plaintext)
      SecureSession.TagBinary -> Frame.Binary(true, plaintext)
      else -> null
    }
  }

  fun decodeTextPayload(frame: Frame.Text): LivewireIncoming.Payload? {
    return envelopeDecoder.decode(frame.readText())?.let { LivewireIncoming.Payload(it) }
  }

  fun decodeLayoutBytes(bytes: ByteArray): LivewireIncoming.Layout {
    return LivewireIncoming.Layout(serializationStrategy.decodeFromByteArray(bytes))
  }

  fun decode(frame: Frame): LivewireIncoming? {
    val plaintextFrame = decryptFrame(frame) ?: return null
    return when (plaintextFrame) {
      is Frame.Text -> decodeTextPayload(plaintextFrame)
      is Frame.Binary -> decodeLayoutBytes(plaintextFrame.readBytes())
      else -> null
    }
  }

  fun encodePayload(payload: Any): Frame {
    val json = when (payload) {
      is UiProtocol -> EnvelopeJson.encodeToString(UiProtocol.serializer(), payload)
      is LivewireAction -> EnvelopeJson.encodeToString(LivewireAction.serializer(), payload)
      else -> EnvelopeJson.encodeToString(payload)
    }
    return tryEncryptFrame(Frame.Text(json))
  }

  fun encodeLayout(node: LayoutNode): Frame {
    val nodeBinary = serializationStrategy.encodeToByteArray(node)
    outgoingSizeReporter(nodeBinary.size.toLong())
    return tryEncryptFrame(Frame.Binary(true, nodeBinary))
  }

  private fun tryEncryptFrame(frame: Frame): Frame {
    if (secureSession == null) return frame

    val encrypted = when (frame) {
      is Frame.Text -> secureSession!!.encryptText(frame.readBytes())
      is Frame.Binary -> secureSession!!.encryptBinary(frame.readBytes())
      else -> secureSession!!.encryptText(frame.readBytes())
    }
    return Frame.Binary(true, encrypted)
  }
}

sealed interface LivewireIncoming {
  data class Payload(val payload: Any) : LivewireIncoming
  data class Layout(val node: LayoutNode) : LivewireIncoming
}
