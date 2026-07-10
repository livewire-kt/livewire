package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.RemoteIcon
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.ChipNode
import com.livewire.ui.widget.ChipStyle
import kotlinx.coroutines.launch

@Composable
internal fun ChipNodeContent(
  node: ChipNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current
  val onClick: () -> Unit = {
    scope.launch {
      eventDispatcher.dispatch(node.action)
    }
  }
  val leadingIcon: (@Composable () -> Unit)? = node.leadingIcon?.let { icon ->
    {
      RemoteIcon(
        vector = icon,
        contentDescription = null,
        modifier = Modifier
          .size(
            when (node.style) {
              ChipStyle.Assist -> AssistChipDefaults.IconSize
              ChipStyle.Filter -> FilterChipDefaults.IconSize
              ChipStyle.Input -> InputChipDefaults.IconSize
              ChipStyle.Suggestion -> SuggestionChipDefaults.IconSize
            }
          ),
      )
    }
  }

  when (node.style) {
    ChipStyle.Assist -> {
      if (node.elevated) {
        ElevatedAssistChip(
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
          leadingIcon = leadingIcon,
        )
      } else {
        AssistChip(
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
          leadingIcon = leadingIcon,
        )
      }
    }

    ChipStyle.Filter -> {
      if (node.elevated) {
        ElevatedFilterChip(
          selected = node.selected,
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
          leadingIcon = leadingIcon,
        )
      } else {
        FilterChip(
          selected = node.selected,
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
          leadingIcon = leadingIcon,
        )
      }
    }

    ChipStyle.Input -> {
      InputChip(
        selected = node.selected,
        onClick = onClick,
        label = { Text(node.label) },
        modifier = modifier.debugFrame(),
        enabled = node.enabled,
        leadingIcon = leadingIcon,
      )
    }

    ChipStyle.Suggestion -> {
      if (node.elevated) {
        ElevatedSuggestionChip(
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
        )
      } else {
        SuggestionChip(
          onClick = onClick,
          label = { Text(node.label) },
          modifier = modifier.debugFrame(),
          enabled = node.enabled,
        )
      }
    }
  }
}
