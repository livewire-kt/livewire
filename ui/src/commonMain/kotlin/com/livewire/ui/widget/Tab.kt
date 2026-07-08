package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.remember
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.vector.ImageVector
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.IntValueChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.graphics.toVectorIcon
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun TabRow(
  selectedTabIndex: Int,
  onTabSelected: IntValueChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  style: TabStyle = TabStyle.Primary,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TabRowNode, Applier<LayoutNode>>(
    factory = { TabRowNode(selectedTabIndex, onTabSelected) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(style, TabRowNode.SetStyle)
      update(selectedTabIndex, TabRowNode.SetSelectedTabIndex)
      update(onTabSelected, TabRowNode.SetOnTabSelected)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class TabRowNode(
  var selectedTabIndex: Int,
  var onTabSelected: IntValueChangeAction,
  var style: TabStyle = TabStyle.Primary,
) : LayoutNode() {

  companion object {
    val SetSelectedTabIndex: TabRowNode.(Int) -> Unit = applier { selectedTabIndex = it }
    val SetOnTabSelected: TabRowNode.(IntValueChangeAction) -> Unit = applier { onTabSelected = it }
    val SetStyle: TabRowNode.(TabStyle) -> Unit = applier { style = it }
  }
}

@LivewireComposable
@Composable
fun Tab(
  text: String? = null,
  modifier: LivewireModifier = LivewireModifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
) {
  val vector = remember(icon) { icon?.toVectorIcon() }
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TabNode, Applier<LayoutNode>>(
    factory = { TabNode(text, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(text, TabNode.SetText)
      set(vector, TabNode.SetIcon)
      set(enabled, TabNode.SetEnabled)
    },
  )
}

@LivewireSerializer
@Serializable
class TabNode(
  var text: String? = null,
  var enabled: Boolean = true,
) : LayoutNode() {

  var icon: VectorIcon? = null

  companion object {
    val SetText: TabNode.(String?) -> Unit = applier { text = it }
    val SetIcon: TabNode.(VectorIcon?) -> Unit = applier { icon = it }
    val SetEnabled: TabNode.(Boolean) -> Unit = applier { enabled = it }
  }
}

@Serializable
enum class TabStyle {
  Primary, Secondary
}
