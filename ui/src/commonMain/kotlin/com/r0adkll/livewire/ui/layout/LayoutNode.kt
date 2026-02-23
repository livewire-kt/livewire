package com.r0adkll.livewire.ui.layout

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

private const val DebugChanges = false

@OptIn(ExperimentalSerializationApi::class)
@Serializable
abstract class LayoutNode(
  val children: MutableList<LayoutNode> = mutableListOf(),
) {

  /**
   * Callback invoked whenever the node in the vector tree is modified in a way that would change
   * the output of the Vector
   */
  @Transient
  internal open var invalidateListener: ((LayoutNode) -> Unit)? = null

  fun invalidate() {
    invalidateListener?.invoke(this)
  }

  fun insertAt(index: Int, instance: LayoutNode) {
    instance.invalidateListener = { invalidate() }
    children.add(index, instance)
    invalidate()
  }

  fun move(from: Int, to: Int, count: Int) {
    if (from == to) return

    for (i in 0 until count) {
      // if "from" is after "to," the from index moves because we're inserting before it
      val fromIndex = if (from > to) from + i else from
      val toIndex = if (from > to) to + i else to + count - 2
      val child = children.removeAt(fromIndex)

      if (DebugChanges) {
        println("$child moved in $this from index $fromIndex to $toIndex")
      }

      children.add(toIndex, child)
    }
    invalidate()
  }

  fun removeAt(index: Int, count: Int) {
    for (i in index + count - 1 downTo index) {
      // Call detach callbacks before removing from _foldedChildren, so the child is still
      // visible to parents traversing downwards, such as when clearing focus.
//      onChildRemoved(_foldedChildren[i])
      val child = children.removeAt(i)
      child.invalidateListener = null
      if (DebugChanges) {
        println("$child removed from $this at index $i")
      }
    }
    invalidate()
  }

  fun removeAll() {
    children.clear()
    invalidate()
  }

}

fun <T : LayoutNode, C> applier(block: T.(C) -> Unit): T.(C) -> Unit {
  return {
    block(it)
    invalidate()
  }
}

@Serializable
class RootNode : LayoutNode()
