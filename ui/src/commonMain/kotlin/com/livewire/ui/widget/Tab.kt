package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.remember
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.vector.ImageVector
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.graphics.toVectorIcon
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowScope
import com.livewire.ui.layout.RowScopeInstance
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun TabRow(
  modifier: LivewireModifier = LivewireModifier,
  style: TabStyle = TabStyle.Primary,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TabRowNode, Applier<LayoutNode>>(
    factory = { TabRowNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(style, TabRowNode.SetStyle)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class TabRowNode(
  var style: TabStyle = TabStyle.Primary,
) : LayoutNode() {

  companion object {
    val SetStyle: TabRowNode.(TabStyle) -> Unit = applier { style = it }
  }
}

@LivewireComposable
@Composable
fun Tab(
  selected: Boolean,
  onClick: ClickAction,
  text: String? = null,
  modifier: LivewireModifier = LivewireModifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  content: (@Composable @LivewireComposable RowScope.() -> Unit)? = null,
) {
  val vector = remember(icon) { icon?.toVectorIcon() }
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TabNode, Applier<LayoutNode>>(
    factory = { TabNode(selected, onClick, text, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(selected, TabNode.SetSelected)
      set(onClick, TabNode.SetOnClick)
      set(text, TabNode.SetText)
      set(vector, TabNode.SetIcon)
      set(enabled, TabNode.SetEnabled)
    },
    content = {
      if (content != null) {
        RowScopeInstance.content()
      }
    },
  )
}

@LivewireSerializer
@Serializable
class TabNode(
  var selected: Boolean,
  var onClick: ClickAction,
  var text: String? = null,
  var enabled: Boolean = true,
) : LayoutNode() {

  var icon: VectorIcon? = null

  companion object {
    val SetSelected: TabNode.(Boolean) -> Unit = applier { selected = it }
    val SetOnClick: TabNode.(ClickAction) -> Unit = applier { onClick = it }
    val SetText: TabNode.(String?) -> Unit = applier { text = it }
    val SetIcon: TabNode.(VectorIcon?) -> Unit = applier { icon = it }
    val SetEnabled: TabNode.(Boolean) -> Unit = applier { enabled = it }
  }
}

@Serializable
enum class TabStyle {
  Primary, Secondary
}
