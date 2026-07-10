package com.livewire.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.livewire.runtime.HostConnectionState
import com.livewire.runtime.discoverymanager.AdbDevice
import com.livewire.runtime.discoverymanager.AndroidApp
import com.livewire.runtime.discoverymanager.DesktopApp
import com.livewire.runtime.discoverymanager.DesktopDevice
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.runtime.discoverymanager.IosApp
import com.livewire.runtime.discoverymanager.IosDevice
import com.livewire.theme.BlackHanSans
import com.livewire.ui.icons.CloseIcon
import com.livewire.ui.icons.DisconnectedIcon
import livewire.host.generated.resources.Res
import livewire.host.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Image as SkiaImage
import kotlin.io.encoding.Base64

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DisconnectedStateLayout(
  apps: List<HostApp>,
  devicesReady: Boolean,
  state: HostConnectionState,
  onConnectClick: (HostApp) -> Unit,
  onDisconnectClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(Modifier.weight(1f))

        Image(
          painterResource(Res.drawable.logo),
          contentDescription = null,
          modifier = Modifier.height(128.dp)
        )

        Spacer(Modifier.weight(1f))

        Text(
          text = "Connect to an application",
          style = MaterialTheme.typography.titleLarge,
          fontFamily = BlackHanSans,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    BoxWithConstraints(
      modifier = Modifier.weight(2f),
      contentAlignment = Alignment.TopCenter,
    ) {
      val selectorWidth = maxOf(maxWidth * 0.6f, 480.dp)

      AppSelector(
        apps = apps,
        devicesReady = devicesReady,
        onConnectClick = onConnectClick,
        onDisconnectClick = onDisconnectClick,
        state = state,
        modifier = Modifier
          .fillMaxHeight()
          .width(selectorWidth),
      )
    }
  }
}

@Composable
private fun AppSelector(
  apps: List<HostApp>,
  devicesReady: Boolean,
  onConnectClick: (HostApp) -> Unit,
  onDisconnectClick: () -> Unit,
  state: HostConnectionState,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(MaterialTheme.shapes.large),
    shape = MaterialTheme.shapes.extraLarge,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
    ),
  ) {
    when {
      !devicesReady -> LoadingState()
      apps.isEmpty() -> EmptyState()
      else -> DeviceList(
        apps = apps,
        onConnectClick = onConnectClick,
        onDisconnectClick = onDisconnectClick,
        state = state,
      )
    }
  }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      LinearProgressIndicator(
        modifier = Modifier.width(100.dp),
        strokeCap = StrokeCap.Round,
      )

      Text(
        text = "Scanning…",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize().padding(32.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = DisconnectedIcon,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
      )

      Text(
        text = "No apps found",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Text(
        text = "Make sure a Livewire-enabled app is running on a connected device.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
      )
    }
  }
}

@Composable
private fun DeviceList(
  apps: List<HostApp>,
  onConnectClick: (HostApp) -> Unit,
  onDisconnectClick: () -> Unit,
  state: HostConnectionState,
  modifier: Modifier = Modifier,
) {
  var selectedApp by remember { mutableStateOf<HostApp?>(null) }
  LaunchedEffect(apps) {
    if (apps.isEmpty()) {
      selectedApp = null
    } else if (selectedApp == null) {
      selectedApp = apps.first()
    }
  }

  val sortedApps = apps.sortedWith(
    compareBy<HostApp> {
      when (it.device) {
        is AdbDevice -> 0
        is IosDevice -> 1
        is DesktopDevice -> 2
      }
    }
      .thenBy { it.device.displayDetail }
      .thenBy { it.displayName }
  )

  Column(modifier.fillMaxWidth()) {
    LazyColumn(
      modifier = Modifier.weight(1f).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(
        count = sortedApps.size,
        key = { sortedApps[it].device.id + sortedApps[it].id }
      ) {
        val app = sortedApps[it]

        AppItem(
          app = app,
          selected = app.id == selectedApp?.id,
          onClick = { selectedApp = app },
        )
      }
    }

    SelectedAppFooter(
      selectedApp = selectedApp,
      onConnectClick = { selectedApp?.let { onConnectClick(it) } },
      onDisconnectClick = onDisconnectClick,
      state = state,
    )
  }
}

@Composable
private fun AppItem(
  app: HostApp,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor = if (selected) {
    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
  } else {
    MaterialTheme.colorScheme.surface
  }
  val borderColor = if (selected) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.large)
      .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.large,
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    tonalElevation = if (selected) 1.dp else 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = app.device.platformIcon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Column(Modifier.weight(1f)) {
        Text(
          text = app.displayName,
          style = MaterialTheme.typography.bodyMedium,
          color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )

        Text(
          text = when (app) {
            is AndroidApp -> app.packageName
            is IosApp -> app.bundleId
            is DesktopApp -> "PID: ${app.processId}"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
          text = app.device.displayDetail,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      AppIcon(app = app)
    }
  }
}

@Composable
private fun AppIcon(
  app: HostApp,
  modifier: Modifier = Modifier,
) {
  val iconBitmap = remember(app.appIcon) {
    app.appIcon?.let { encoded ->
      runCatching {
        SkiaImage.makeFromEncoded(Base64.decode(encoded)).toComposeImageBitmap()
      }.getOrNull()
    }
  }

  iconBitmap?.let { bitmap ->
    Image(
      bitmap = bitmap,
      contentDescription = null,
      modifier = modifier
        .size(40.dp)
        .clip(MaterialTheme.shapes.medium),
    )
  }
}

@Composable
private fun SelectedAppFooter(
  selectedApp: HostApp?,
  onConnectClick: () -> Unit,
  onDisconnectClick: () -> Unit,
  state: HostConnectionState,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    HorizontalDivider(
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )

    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = if (selectedApp != null) "${selectedApp.displayName} • ${selectedApp.device.displayDetail}" else "No app selected",
          style = MaterialTheme.typography.titleMedium,
          color = if (selectedApp != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ConnectButton(
        state = state,
        onConnectClick = onConnectClick,
        onDisconnectClick = onDisconnectClick,
        enabled = selectedApp != null,
      )
    }
  }
}

@Composable
fun ConnectButton(
  state: HostConnectionState,
  onConnectClick: () -> Unit,
  onDisconnectClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val isLoading = state != Disconnected

  AnimatedContent(
    targetState = isLoading,
    modifier = modifier,
    transitionSpec = {
      (fadeIn(tween(220, delayMillis = 90)) togetherWith fadeOut(tween(90)))
        .using(
          SizeTransform(clip = false) { _, _ ->
            spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMedium,
            )
          }
        )
    },
    contentAlignment = Alignment.Center,
    label = "ConnectButton",
  ) { loading ->
    if (loading) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(56.dp),
      ) {
        CircularProgressIndicator(
          modifier = Modifier.fillMaxSize(),
          strokeWidth = 2.5.dp,
          color = MaterialTheme.colorScheme.primary,
        )

        Button(
          onClick = onDisconnectClick,
          modifier = Modifier.size(44.dp),
          shape = CircleShape,
          contentPadding = PaddingValues(0.dp),
        ) {
          Icon(
            imageVector = CloseIcon,
            contentDescription = "Cancel",
            modifier = Modifier.size(18.dp),
          )
        }
      }
    } else {
      Button(
        onClick = onConnectClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
      ) {
        Text("Connect")
      }
    }
  }
}
