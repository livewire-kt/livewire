package com.r0adkll.livewire.ui.composition

import androidx.compose.runtime.AbstractApplier
import com.r0adkll.livewire.ui.layout.LayoutNode

class LivewireApplier(
  root: LayoutNode,
  private val onApplied: (LayoutNode) -> Unit,
) : AbstractApplier<LayoutNode>(root) {

  override fun insertTopDown(index: Int, instance: LayoutNode) {
    // ignored?
  }

  override fun insertBottomUp(index: Int, instance: LayoutNode) {
    current.insertAt(index, instance)
  }

  override fun remove(index: Int, count: Int) {
    current.removeAt(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    current.move(from, to, count)
  }

  override fun onClear() {
    root.removeAll()
  }

  override fun onEndChanges() {
    onApplied(root)
  }
}
