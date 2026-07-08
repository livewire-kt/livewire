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
import com.livewire.ui.layout.ColumnScope
import com.livewire.ui.layout.ColumnScopeInstance
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
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
    val SetExpanded: DropdownMenuNode.(Boolean) -> Unit = applier { expanded = it }
    val SetOnDismissRequest: DropdownMenuNode.(ClickAction) -> Unit = applier { onDismissRequest = it }
  }
}

@LivewireComposable
@Composable
fun DropdownMenuItem(
  text: String,
  onClick: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
  enabled: Boolean = true,
) {
  val leadingVector = remember(leadingIcon) { leadingIcon?.toVectorIcon() }
  val trailingVector = remember(trailingIcon) { trailingIcon?.toVectorIcon() }
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<DropdownMenuItemNode, Applier<LayoutNode>>(
    factory = { DropdownMenuItemNode(text, onClick) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(text, DropdownMenuItemNode.SetText)
      update(onClick, DropdownMenuItemNode.SetOnClick)
      set(leadingVector, DropdownMenuItemNode.SetLeadingIcon)
      set(trailingVector, DropdownMenuItemNode.SetTrailingIcon)
      set(enabled, DropdownMenuItemNode.SetEnabled)
    },
  )
}

@LivewireSerializer
@Serializable
class DropdownMenuItemNode(
  var text: String,
  var onClick: ClickAction,
  var enabled: Boolean = true,
) : LayoutNode() {

  var leadingIcon: VectorIcon? = null

  var trailingIcon: VectorIcon? = null

  companion object {
    val SetText: DropdownMenuItemNode.(String) -> Unit = applier { text = it }
    val SetOnClick: DropdownMenuItemNode.(ClickAction) -> Unit = applier { onClick = it }
    val SetLeadingIcon: DropdownMenuItemNode.(VectorIcon?) -> Unit = applier { leadingIcon = it }
    val SetTrailingIcon: DropdownMenuItemNode.(VectorIcon?) -> Unit = applier { trailingIcon = it }
    val SetEnabled: DropdownMenuItemNode.(Boolean) -> Unit = applier { enabled = it }
  }
}
