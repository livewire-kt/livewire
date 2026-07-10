package com.livewire.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.livewire.host.ui.debugFrame
import com.livewire.runtime.LivewireHost
import com.livewire.theme.BlackHanSans
import com.livewire.theme.LivewireThemeContent
import com.livewire.ui.icons.Create
import com.livewire.ui.icons.DefaultPlugin
import com.livewire.ui.icons.SystemReport
import com.livewire.ui.theme.LivewireTheme
import livewire.host.generated.resources.Res
import livewire.host.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

// FIXME: Update these with actual docsite urls when published
private const val SETUP_PLUGINS_DOC_URL = "https://github.com/livewire-kt/livewire"
private const val CREATE_PLUGINS_DOC_URL = "https://github.com/livewire-kt/livewire"
private const val FEEDBACK_URL = "https://github.com/livewire-kt/livewire/issues"

@Composable
internal fun EmptyPluginLayout(
  onOpenUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {

    Image(
      painter = painterResource(Res.drawable.logo),
      contentDescription = null,
      modifier = Modifier.height(128.dp)
    )

    Spacer(Modifier.height(24.dp))

    Text(
      text = "No plugin selected",
      fontFamily = BlackHanSans,
      style = MaterialTheme.typography.titleLarge,
    )

    Spacer(Modifier.height(48.dp))

    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

      EmptyTipAction(
        icon = DefaultPlugin,
        title = "Setup default plugins",
        subtitle = "Learn how to integrate Livewire's default plugins into your application.",
        onClick = {
          onOpenUrl(SETUP_PLUGINS_DOC_URL)
        }
      )

      EmptyTipAction(
        icon = Create,
        title = "Create your own plugin",
        subtitle = "Get started on how to customize Livewire to your app's need",
        onClick = {
          onOpenUrl(CREATE_PLUGINS_DOC_URL)
        }
      )

      EmptyTipAction(
        icon = SystemReport,
        title = "Contributing and feedback",
        subtitle = "Report issues, contribute improvements and help us improve Livewire",
        onClick = {
          onOpenUrl(FEEDBACK_URL)
        }
      )
    }
  }
}

@Composable
private fun EmptyTipAction(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(16.dp)
  Row(
    modifier = modifier
      .padding(horizontal = 16.dp)
      .sizeIn(minWidth = 400.dp, maxWidth = 400.dp)
      .clip(shape)
      .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
      .clickable(onClick = onClick)
      .padding(
        horizontal = 16.dp,
        vertical = 12.dp,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CompositionLocalProvider(
      LocalContentColor provides MaterialTheme.colorScheme.onSurface,
    ) {

      Image(
        icon,
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
        modifier = Modifier
          .padding(8.dp)
          .size(24.dp)
          .debugFrame()
      )

      Spacer(Modifier.width(16.dp))

      Column(
        modifier = Modifier
          .debugFrame(),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.secondary,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

@Preview(
  widthDp = 1280,
  heightDp = 720,
)
@Composable
fun EmptyPluginLayoutPreview() {
  val host = remember { LivewireHost() }
  LivewireThemeContent(
    theme = LivewireTheme(),
    darkMode = true,
    host = host,
  ) {
    Surface {
      EmptyPluginLayout(
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
