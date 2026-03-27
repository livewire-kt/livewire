package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.ColumnScope
import com.r0adkll.livewire.ui.layout.ColumnScopeInstance
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun DropdownMenu(
  expanded: Boolean,
  onDismissRequest: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  content: @Composable @LivewireComposable ColumnScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<DropdownMenuNode, Applier<LayoutNode>>(
    factory = { DropdownMenuNode(expanded, onDismissRequest) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(expanded, DropdownMenuNode.SetExpanded)
      update(onDismissRequest, DropdownMenuNode.SetOnDismissRequest)
    },
    content = { ColumnScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class DropdownMenuNode(
  var expanded: Boolean,
  var onDismissRequest: ClickAction,
) : LayoutNode() {

  companion object {
    val SetExpanded: DropdownMenuNode.(Boolean) -> Unit = { expanded = it }
    val SetOnDismissRequest: DropdownMenuNode.(ClickAction) -> Unit = { onDismissRequest = it }
  }
}

@LivewireComposable
@Composable
fun DropdownMenuItem(
  text: String,
  onClick: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  leadingIconData: String? = null,
  trailingIconData: String? = null,
  enabled: Boolean = true,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<DropdownMenuItemNode, Applier<LayoutNode>>(
    factory = { DropdownMenuItemNode(text, onClick) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(text, DropdownMenuItemNode.SetText)
      update(onClick, DropdownMenuItemNode.SetOnClick)
      set(leadingIconData, DropdownMenuItemNode.SetLeadingIconData)
      set(trailingIconData, DropdownMenuItemNode.SetTrailingIconData)
      set(enabled, DropdownMenuItemNode.SetEnabled)
    },
  )
}

@LivewireSerializer
@Serializable
class DropdownMenuItemNode(
  var text: String,
  var onClick: ClickAction,
  var leadingIconData: String? = null,
  var trailingIconData: String? = null,
  var enabled: Boolean = true,
) : LayoutNode() {

  companion object {
    val SetText: DropdownMenuItemNode.(String) -> Unit = { text = it }
    val SetOnClick: DropdownMenuItemNode.(ClickAction) -> Unit = { onClick = it }
    val SetLeadingIconData: DropdownMenuItemNode.(String?) -> Unit = { leadingIconData = it }
    val SetTrailingIconData: DropdownMenuItemNode.(String?) -> Unit = { trailingIconData = it }
    val SetEnabled: DropdownMenuItemNode.(Boolean) -> Unit = { enabled = it }
  }
}
