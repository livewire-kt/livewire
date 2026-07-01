package com.r0adkll.livewire.ui.composition

import androidx.compose.runtime.AbstractApplier
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.LayoutNodePatch

class LivewireApplier(
  root: LayoutNode,
  private val onOutput: (LivewireOutput) -> Unit,
  private val serializationStrategy: LayoutNodeSerializationStrategy? = null,
) : AbstractApplier<LayoutNode>(root) {

  private var isFirstFrame = true
  private val pendingOperations = mutableListOf<LayoutNodePatch>()
  private val insertedNodeIds = mutableSetOf<Long>()
  private val shadowProperties = mutableMapOf<Long, ByteArray>()

  override fun insertTopDown(index: Int, instance: LayoutNode) = Unit

  override fun insertBottomUp(index: Int, instance: LayoutNode) {
    current.insertAt(index, instance)
    if (!isFirstFrame && serializationStrategy != null) {
      if (current.nodeId in shadowProperties) {
        pendingOperations += LayoutNodePatch.InsertAt(current.nodeId, index, instance)
      }
      instance.collectAllNodeIds(insertedNodeIds)
    }
  }

  override fun remove(index: Int, count: Int) {
    if (!isFirstFrame && serializationStrategy != null) {
      for (i in index until index + count) {
        current.children[i].removeFromShadow(shadowProperties)
      }
      pendingOperations += LayoutNodePatch.RemoveAt(current.nodeId, index, count)
    }
    current.removeAt(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    if (!isFirstFrame && serializationStrategy != null) {
      pendingOperations += LayoutNodePatch.Move(current.nodeId, from, to, count)
    }
    current.move(from, to, count)
  }

  override fun onClear() {
    if (!isFirstFrame && serializationStrategy != null) {
      root.children.forEach { it.removeFromShadow(shadowProperties) }
      pendingOperations += LayoutNodePatch.Clear(root.nodeId)
    }
    root.removeAll()
  }

  override fun onEndChanges() {
    when {
      serializationStrategy == null -> onOutput(LivewireOutput.FullTree(root))
      isFirstFrame -> {
        isFirstFrame = false
        root.updateShadowProperties(insertedNodeIds, shadowProperties, serializationStrategy, pendingOperations)
        onOutput(LivewireOutput.FullTree(root))
      }
      else -> {
        root.updateShadowProperties(insertedNodeIds, shadowProperties, serializationStrategy, pendingOperations)
        onOutput(LivewireOutput.Patches(pendingOperations.toList()))
      }
    }

    pendingOperations.clear()
    insertedNodeIds.clear()
  }
}

private fun LayoutNode.collectAllNodeIds(into: MutableSet<Long>) {
  into.add(nodeId)
  children.forEach { it.collectAllNodeIds(into) }
}

private fun LayoutNode.removeFromShadow(shadow: MutableMap<Long, ByteArray>) {
  shadow.remove(nodeId)
  children.forEach { it.removeFromShadow(shadow) }
}

private fun LayoutNode.updateShadowProperties(
  insertedIds: Set<Long>,
  shadowProperties: MutableMap<Long, ByteArray>,
  strategy: LayoutNodeSerializationStrategy,
  patches: MutableList<LayoutNodePatch>,
) {
  val alreadyShadowed = nodeId in shadowProperties
  if (propertiesDirty || !alreadyShadowed) {
    val currentBytes = encodePropertiesOnly(strategy)
    if (nodeId !in insertedIds && alreadyShadowed) {
      val shadowBytes = shadowProperties[nodeId]
      if (shadowBytes != null && !currentBytes.contentEquals(shadowBytes)) {
        patches += LayoutNodePatch.UpdateNode(nodeId, currentBytes)
      }
    }
    shadowProperties[nodeId] = currentBytes
    propertiesDirty = false
  }
  children.forEach { it.updateShadowProperties(insertedIds, shadowProperties, strategy, patches) }
}

private fun LayoutNode.encodePropertiesOnly(strategy: LayoutNodeSerializationStrategy): ByteArray {
  // temporarily sub in an empty child list to ensure only properties are encoded
  val saved = children
  children = EmptyChildList
  return try {
    strategy.encodeToByteArray(this)
  } finally {
    children = saved
  }
}

private val EmptyChildList: MutableList<LayoutNode> = mutableListOf()
