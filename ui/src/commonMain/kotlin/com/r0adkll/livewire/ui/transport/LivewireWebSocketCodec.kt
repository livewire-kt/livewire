package com.r0adkll.livewire.ui.transport

import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.data.LivewireUiJson
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.layout.LayoutNode
import io.ktor.utils.io.core.toByteArray
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText

class LivewireWebSocketCodec(decoders: Collection<PayloadDecoder<*>>) {
  private val envelopeDecoder = EnvelopeDecoder(payloadDecoders = decoders.toSet())

  var serializationStrategy: LayoutNodeSerializationStrategy =
    LayoutNodeSerializationStrategy.Default

  suspend fun decode(frame: Frame): LivewireIncoming? {
    return when (frame) {
      is Frame.Text -> {
        val text = frame.readText()
        val payload = envelopeDecoder.decode(text)
        if (payload != null) {
          LivewireIncoming.Payload(payload)
        } else {
          null
        }
      }

      is Frame.Binary -> {
        val bytes = frame.readBytes()
        val layoutNode = serializationStrategy.decodeFromByteArray(bytes)
        LivewireIncoming.Layout(layoutNode)
      }

      else -> null
    }
  }

  fun encodePayload(payload: Any): Frame.Text {
    val json = when (payload) {
      is UiProtocol -> EnvelopeJson.encodeToString(UiProtocol.serializer(), payload)
      is LivewireAction -> EnvelopeJson.encodeToString(LivewireAction.serializer(), payload)
      else -> EnvelopeJson.encodeToString(payload)
    }
    return Frame.Text(json)
  }

  fun encodeLayout(node: LayoutNode): Frame.Binary {
    val nodeBinary = serializationStrategy.encodeToByteArray(node)
    return Frame.Binary(true, nodeBinary)
  }
}

sealed interface LivewireIncoming {
  data class Payload(val payload: Any) : LivewireIncoming
  data class Layout(val node: LayoutNode) : LivewireIncoming
}
