package com.livewire.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.livewire.host.ui.DebugNodes
import com.livewire.runtime.HostConnectionState
import com.livewire.runtime.HostConnectionState.Connected
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.ui.icons.BugReport
import com.livewire.ui.icons.DarkMode
import com.livewire.ui.icons.LightMode
import com.livewire.ui.icons.MenuOpen
import com.livewire.ui.icons.NetworkCheck

@Composable
internal fun AppTopBar(
  darkMode: Boolean,
  onDarkModeChanged: (Boolean) -> Unit,
  hostConnectionState: HostConnectionState,
  selectedApp: HostApp?,
  onDisconnectClick: () -> Unit,
  onNavigationItemClick: () -> Unit,
  onNetworkMeterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .height(48.dp)
      .fillMaxWidth(),
    shadowElevation = 2.dp,
    tonalElevation = 1.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp),
    ) {
      IconButton(
        onClick = onNavigationItemClick,
        enabled = hostConnectionState == Connected,
      ) {
        Icon(
          MenuOpen,
          contentDescription = "Toggle menu",
          modifier = Modifier.size(20.dp),
        )
      }

      ConnectionStatusChip(
        state = hostConnectionState,
        selectedApp = selectedApp,
        onDisconnectClick = onDisconnectClick,
      )

      Spacer(Modifier.weight(1f))

      IconButton(onClick = onNetworkMeterClick) {
        Icon(
          NetworkCheck,
          contentDescription = "Open network meter",
          modifier = Modifier.size(18.dp),
        )
      }

      Spacer(Modifier.width(8.dp))

      // TODO: Hide behind a 'Debug' build flag
      Switch(
        checked = DebugNodes,
        onCheckedChange = { DebugNodes = it },
        thumbContent = {
          Icon(
            BugReport,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        },
      )

      Spacer(Modifier.width(8.dp))

      Switch(
        checked = darkMode,
        onCheckedChange = onDarkModeChanged,
        thumbContent = {
          Icon(
            if (darkMode) DarkMode else LightMode,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
            tint = Color.White
          )
        },
      )

      Spacer(Modifier.width(8.dp))
    }
  }
}
