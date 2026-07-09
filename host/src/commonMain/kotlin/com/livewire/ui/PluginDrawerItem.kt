package com.livewire.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PluginDrawerItem(
  expanded: Boolean,
  selected: Boolean,
  info: PluginInfo,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor by animateColorAsState(
    if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
  )
  val contentColor by animateColorAsState(
    if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
  )

  Surface(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    color = containerColor,
    contentColor = contentColor,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(8.dp)
        .animateContentSize(),
    ) {
      val iconContainerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
      )
      val iconContentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
      )

      Box(
        modifier = Modifier
          .size(32.dp)
          .background(
            color = iconContainerColor,
            shape = MaterialTheme.shapes.small,
          ),
        contentAlignment = Alignment.Center,
      ) {
        PluginIcon(
          info = info,
          color = iconContentColor,
          modifier = Modifier.size(18.dp),
        )
      }

      AnimatedVisibility(
        visible = expanded,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = info.title,
          style = MaterialTheme.typography.titleMediumEmphasized,
          modifier = Modifier
            .padding(horizontal = 16.dp),
        )
      }
    }
  }
}

@Composable
private fun PluginIcon(
  info: PluginInfo,
  color: Color,
  modifier: Modifier = Modifier,
) {
  val icon = info.icon ?: return
  Icon(
    imageVector = remember(icon) { icon.toImageVector() },
    contentDescription = null,
    tint = color,
    modifier = modifier,
  )
}
