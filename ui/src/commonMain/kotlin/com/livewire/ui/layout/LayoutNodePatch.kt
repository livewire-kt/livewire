package com.livewire.ui.layout

import kotlinx.serialization.Serializable

@Serializable
sealed class LayoutNodePatch {

  @Serializable
  data class InsertAt(
    val parentNodeId: Long,
    val index: Int,
    val node: LayoutNode,
  ) : LayoutNodePatch()

  @Serializable
  data class RemoveAt(
    val parentNodeId: Long,
    val index: Int,
    val count: Int,
  ) : LayoutNodePatch()

  @Serializable
  data class Move(
    val parentNodeId: Long,
    val from: Int,
    val to: Int,
    val count: Int,
  ) : LayoutNodePatch()

  @Serializable
  data class Clear(val nodeId: Long) : LayoutNodePatch()

  @Serializable
  data class UpdateNode(
    val nodeId: Long,
    val propertyBytes: ByteArray,
  ) : LayoutNodePatch() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is UpdateNode) return false
      return nodeId == other.nodeId && propertyBytes.contentEquals(other.propertyBytes)
    }

    override fun hashCode(): Int = 31 * nodeId.hashCode() + propertyBytes.contentHashCode()
  }
}

@Serializable
internal data class LayoutNodePatchList(val patches: List<LayoutNodePatch> = emptyList())
