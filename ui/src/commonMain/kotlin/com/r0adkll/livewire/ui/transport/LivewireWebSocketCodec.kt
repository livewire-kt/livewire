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

  suspend fun decode(frame: Frame): LivewireIncoming? {
    val plaintextFrame = if (secureSession != null && frame is Frame.Binary) {
      val (tag, plaintext) = secureSession!!.decrypt(frame.readBytes())
      when (tag) {
        SecureSession.TagText -> Frame.Text(true, plaintext)
        SecureSession.TagBinary -> Frame.Binary(true, plaintext)
        else -> frame
      }
    } else {
      frame
    }

    return when (plaintextFrame) {
      is Frame.Text -> {
        envelopeDecoder.decode(plaintextFrame.readText())?.let { LivewireIncoming.Payload(it) }
      }

      is Frame.Binary -> {
        val bytes = plaintextFrame.readBytes()
        val layoutNode = serializationStrategy.decodeFromByteArray(bytes)
        LivewireIncoming.Layout(layoutNode)
      }

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
