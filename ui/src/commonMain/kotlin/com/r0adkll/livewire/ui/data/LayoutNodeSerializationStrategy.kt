package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.LayoutNodePatch
import com.r0adkll.livewire.ui.layout.LayoutNodePatchList
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
  fun encodePatchList(patches: List<LayoutNodePatch>): ByteArray
  fun decodePatchList(bytes: ByteArray): List<LayoutNodePatch>

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    val Default: LayoutNodeSerializationStrategy
      get() = ProtobufLayoutNodeSerializationStrategy()
  }
}

class JsonLayoutNodeSerializationStrategy(
  private val json: Json = LivewireUiJson,
) : LayoutNodeSerializationStrategy {
  private val debugJson by lazy {
    Json(json) {
      prettyPrint = true
      prettyPrintIndent = "  "
    }
  }

  override fun encodeToByteArray(layoutNode: LayoutNode): ByteArray {
    return json.encodeToString(layoutNode).encodeToByteArray()
  }

  override fun decodeFromByteArray(bytes: ByteArray): LayoutNode {
    val layoutNode = json.decodeFromString<LayoutNode>(bytes.decodeToString())
    // Uncomment to debug nodes
//    println(
//      debugJson.encodeToString(layoutNode)
//    )
    return layoutNode
  }

  override fun encodePatchList(patches: List<LayoutNodePatch>): ByteArray {
    return json.encodeToString(LayoutNodePatchList(patches)).encodeToByteArray()
  }

  override fun decodePatchList(bytes: ByteArray): List<LayoutNodePatch> {
    return json.decodeFromString<LayoutNodePatchList>(bytes.decodeToString()).patches
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

  override fun encodePatchList(patches: List<LayoutNodePatch>): ByteArray {
    return protoBuf.encodeToByteArray(LayoutNodePatchList(patches))
  }

  override fun decodePatchList(bytes: ByteArray): List<LayoutNodePatch> {
    return protoBuf.decodeFromByteArray<LayoutNodePatchList>(bytes).patches
  }
}
