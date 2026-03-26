package com.r0adkll.livewire.ui.composables


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.HostConnectionState.Connected
import com.r0adkll.livewire.runtime.HostConnectionState.Disconnected
import com.r0adkll.livewire.runtime.HostConnectionState.Error
import com.r0adkll.livewire.runtime.HostConnectionState.Forwarding
import com.r0adkll.livewire.runtime.HostConnectionState.Listening
import com.r0adkll.livewire.runtime.discoverymanager.HostApp
import com.r0adkll.livewire.ui.icons.CloseIcon
import com.r0adkll.livewire.ui.icons.ConnectedIcon
import com.r0adkll.livewire.ui.icons.DisconnectedIcon

@Composable
internal fun ConnectionStatusChip(
  state: HostConnectionState,
  selectedApp: HostApp?,
  onDisconnectClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val statusColor by animateColorAsState(
    when (state) {
      Connected -> Color(0xff118F00)
      Forwarding, Listening -> Color(0xffD4A017)
      Error -> MaterialTheme.colorScheme.error
      Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
    },
  )

  val pulse = if (state == Forwarding || state == Listening) {
    val infiniteTransition = rememberInfiniteTransition()
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 0.5f,
      animationSpec = infiniteRepeatable(
        animation = tween(800),
        repeatMode = RepeatMode.Reverse,
      ),
    ).value
  } else {
    1f
  }

  val chipBackground by animateColorAsState(statusColor.copy(alpha = 0.1f))

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(chipBackground)
      .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      imageVector = if (state == Connected) ConnectedIcon else DisconnectedIcon,
      contentDescription = null,
      tint = statusColor,
      modifier = Modifier
        .size(16.dp)
        .alpha(pulse),
    )

    AnimatedVisibility(
      visible = state == Connected && selectedApp != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      selectedApp?.let { app ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = app.device.platformIcon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Text(
            text = app.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )

          Text(
            text = app.device.displayDetail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    if (state != Connected) {
      Text(
        text = when (state) {
          Forwarding -> "Forwarding…"
          Listening -> "Listening…"
          Error -> "Error"
          Disconnected -> "Disconnected"
        },
        style = MaterialTheme.typography.labelMedium,
        color = statusColor,
        modifier = Modifier.alpha(pulse),
      )

      Spacer(Modifier.width(6.dp))
    }

    AnimatedVisibility(
      visible = state == Connected,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      IconButton(
        onClick = onDisconnectClick,
        modifier = Modifier.size(28.dp),
        colors = IconButtonDefaults.iconButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      ) {
        Icon(
          CloseIcon,
          contentDescription = "Disconnect",
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}
