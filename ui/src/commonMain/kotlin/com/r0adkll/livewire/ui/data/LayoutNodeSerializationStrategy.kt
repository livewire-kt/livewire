package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.ui.layout.LayoutNode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
enum class LayoutNodeSerialization {
  Json,
  Protobuf;

  @OptIn(ExperimentalSerializationApi::class)
  fun toStrategy(): LayoutNodeSerializationStrategy = when (this) {
    Json -> JsonLayoutNodeSerializationStrategy()
    Protobuf -> ProtobufLayoutNodeSerializationStrategy()
  }
}

sealed interface LayoutNodeSerializationStrategy {
  fun encodeToByteArray(layoutNode: LayoutNode): ByteArray
  fun decodeFromByteArray(bytes: ByteArray): LayoutNode

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    val Default: LayoutNodeSerializationStrategy
      get() = ProtobufLayoutNodeSerializationStrategy()
  }
}

class JsonLayoutNodeSerializationStrategy(
  private val json: Json = LivewireUiJson,
) : LayoutNodeSerializationStrategy {

  override fun encodeToByteArray(layoutNode: LayoutNode): ByteArray {
    return json.encodeToString(layoutNode).encodeToByteArray()
  }

  override fun decodeFromByteArray(bytes: ByteArray): LayoutNode {
    return json.decodeFromString<LayoutNode>(bytes.decodeToString())
  }
}

@OptIn(ExperimentalSerializationApi::class)
class ProtobufLayoutNodeSerializationStrategy(
  private val protoBuf: ProtoBuf = LivewireUiProtobuf,
) : LayoutNodeSerializationStrategy {

  override fun encodeToByteArray(layoutNode: LayoutNode): ByteArray {
    return protoBuf.encodeToByteArray(layoutNode)
  }

  override fun decodeFromByteArray(bytes: ByteArray): LayoutNode {
    return protoBuf.decodeFromByteArray(bytes)
  }
}
