package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.toMutableStateList
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
abstract class LayoutNode(
  var children: MutableList<LayoutNode> = mutableListOf(),
) {

  companion object {
    val SetModifier: LayoutNode.(LivewireModifier) -> Unit = { modifier = it }
    val SetCompositeKeyHash: LayoutNode.(Long) -> Unit = { compositeKeyHash = it }
  }

  val nodeId: Long = nextNodeId()

  var modifier: LivewireModifier = LivewireModifier

  var compositeKeyHash: Long = 0

  fun insertAt(index: Int, instance: LayoutNode) {
    children.add(index, instance)
  }

  fun move(from: Int, to: Int, count: Int) {
    if (from == to) return

    for (i in 0 until count) {
      // if "from" is after "to," the from index moves because we're inserting before it
      val fromIndex = if (from > to) from + i else from
      val toIndex = if (from > to) to + i else to - 1
      val child = children.removeAt(fromIndex)

      if (DebugChanges) {
        println("$child moved in $this from index $fromIndex to $toIndex")
      }

      children.add(toIndex, child)
    }
  }

  fun removeAt(index: Int, count: Int) {
    for (i in index + count - 1 downTo index) {
      // Call detach callbacks before removing from _foldedChildren, so the child is still
      // visible to parents traversing downwards, such as when clearing focus.
//      onChildRemoved(_foldedChildren[i])
      val child = children.removeAt(i)
      if (DebugChanges) {
        println("$child removed from $this at index $i")
      }
    }
  }

  fun removeAll() {
    children.clear()
  }

  // TODO: not sure where to put this - really only needed in hosts plus tests
  fun makeObservable() {
    children = children.toMutableStateList()
    children.forEach { it.makeObservable() }
  }
}

@LivewireSerializer
@Serializable
class RootNode : LayoutNode()

private const val DebugChanges = false

private var nodeIdCounter = 0L
internal fun nextNodeId() = ++nodeIdCounter
