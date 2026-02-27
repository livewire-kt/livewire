package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.IntValueChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.theme.LivewireTheme
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
  ReusableComposeNode<TabRowNode, Applier<LayoutNode>>(
    factory = { TabRowNode(selectedTabIndex, onTabSelected) },
    update = {
      set(modifier, LayoutNode.SetModifier)
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
  iconData: String? = null,
  enabled: Boolean = true,
) {
  ReusableComposeNode<TabNode, Applier<LayoutNode>>(
    factory = { TabNode(text, iconData, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(text, TabNode.SetText)
      set(iconData, TabNode.SetIconData)
      set(enabled, TabNode.SetEnabled)
    },
  )
}

@LivewireSerializer
@Serializable
class TabNode(
  var text: String? = null,
  var iconData: String? = null,
  var enabled: Boolean = true,
) : LayoutNode() {

  companion object {
    val SetText: TabNode.(String?) -> Unit = applier { text = it }
    val SetIconData: TabNode.(String?) -> Unit = applier { iconData = it }
    val SetEnabled: TabNode.(Boolean) -> Unit = applier { enabled = it }
  }
}

@Serializable
enum class TabStyle {
  Primary, Secondary
}
