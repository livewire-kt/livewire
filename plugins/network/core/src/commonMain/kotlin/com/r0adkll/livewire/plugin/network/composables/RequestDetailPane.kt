package com.r0adkll.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.network.data.NetworkEvent
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.actions.IntValueChangeAction
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.plugin.network.ui.Icons
import com.r0adkll.livewire.ui.actions.checkedChangeAction
import com.r0adkll.livewire.ui.actions.intValueChangeAction
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Arrangement
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.util.asReadableBytes
import com.r0adkll.livewire.ui.widget.ButtonGroupDefaults
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.HorizontalDivider
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Tab
import com.r0adkll.livewire.ui.widget.TabRow
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle
import com.r0adkll.livewire.ui.widget.ToggleButton

@Composable
internal fun RequestDetailPane(
  event: NetworkEvent,
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  onClose: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
  ) {
    // Header bar
    Surface(
      shadowElevation = 4.dp
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = LivewireModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = "Request Detail",
            style = TextStyle.TitleSmall,
            modifier = LivewireModifier.weight(1f).padding(left = 8.dp),
          )
          IconButton(action = onClose) {
            Icon(svgData = Icons.Close)
          }
        }

        Row(
          horizontalArrangement = Arrangement.SpacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
          modifier = LivewireModifier
            .padding(horizontal = 16.dp)
        ) {
          ToggleButton(
            checked = selectedTab == 0,
            onCheckedChange = checkedChangeAction("overview") {
              onTabSelected(0)
            },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            size = ButtonSize.ExtraSmall,
            modifier = LivewireModifier.weight(1f),
          ) {
            Text("Overview")
          }
          ToggleButton(
            checked = selectedTab == 1,
            onCheckedChange = checkedChangeAction("headers") {
              onTabSelected(1)
            },
            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
            size = ButtonSize.ExtraSmall,
            modifier = LivewireModifier.weight(1f),
          ) {
            Text("Headers")
          }
          ToggleButton(
            checked = selectedTab == 2,
            onCheckedChange = checkedChangeAction("body") {
              onTabSelected(2)
            },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            size = ButtonSize.ExtraSmall,
            modifier = LivewireModifier.weight(1f),
          ) {
            Text("Body")
          }
        }

        Spacer(LivewireModifier.height(8.dp))
      }
    }

    // Tab content
    Column(
      modifier = LivewireModifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll()
        .padding(12.dp),
    ) {
      when (selectedTab) {
        0 -> OverviewTab(event)
        1 -> HeadersTab(event)
        2 -> BodyTab(event)
      }
    }
  }
}

@Composable
private fun OverviewTab(event: NetworkEvent) {
  Column(
    modifier = LivewireModifier,
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = LivewireTheme.colorScheme.secondaryContainer,
      modifier = LivewireModifier.fillMaxWidth(),
    ) {
      Text(
        text = event.request.url,
        style = TextStyle.LabelLarge,
        modifier = LivewireModifier.padding(
          horizontal = 12.dp,
          vertical = 8.dp,
        )
      )
    }

    Spacer(LivewireModifier.height(8.dp))

    Row {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = LivewireTheme.colorScheme.primaryContainer,
      ) {
        Text(
          text = event.request.method.uppercase(),
          style = TextStyle.LabelSmall,
          fontWeight = FontWeight.Bold.weight,
          modifier = LivewireModifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp,
          )
        )
      }

      event.response?.let { response ->
        Spacer(LivewireModifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = LivewireTheme.colorScheme.tertiaryContainer,
        ) {
          Text(
            text = response.statusCode.toString(),
            style = TextStyle.LabelSmall,
            fontWeight = FontWeight.Bold.weight,
            modifier = LivewireModifier.padding(
              horizontal = 16.dp,
              vertical = 8.dp,
            )
          )
        }
      }

      event.response?.contentLength?.let { contentLength ->
        Spacer(LivewireModifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = LivewireTheme.colorScheme.tertiaryContainer,
        ) {
          Text(
            text = contentLength.asReadableBytes(),
            style = TextStyle.LabelSmall,
            fontWeight = FontWeight.Bold.weight,
            modifier = LivewireModifier.padding(
              horizontal = 16.dp,
              vertical = 8.dp,
            )
          )
        }
      }
    }
  }

//  DetailRow("URL", event.request.url)
//  DetailRow("Method", event.request.method)
//  event.response?.let { response ->
//    DetailRow("Status", response.statusCode.toString())
//  }
//  event.durationMs?.let { duration ->
//    DetailRow("Duration", "${duration}ms")
//  }
//  event.request.contentType?.let { contentType ->
//    DetailRow("Request Content-Type", contentType)
//  }
//  event.request.contentLength?.let { contentLength ->
//    DetailRow("Request Content-Length", "$contentLength bytes")
//  }
//  event.response?.contentType?.let { contentType ->
//    DetailRow("Response Content-Type", contentType)
//  }
//  event.response?.contentLength?.let { contentLength ->
//    DetailRow("Response Content-Length", "$contentLength bytes")
//  }
//  event.error?.let { error ->
//    DetailRow("Error", error)
//  }
}

@Composable
private fun HeadersTab(event: NetworkEvent) {
  Text(
    text = "Request Headers",
    style = TextStyle.TitleSmall,
    modifier = LivewireModifier.padding(bottom = 8.dp),
  )
  if (event.request.headers.isEmpty()) {
    Text(
      text = "No headers",
      style = TextStyle.BodySmall,
      color = LivewireTheme.colorScheme.onSurfaceVariant,
    )
  } else {
    event.request.headers.forEach { (key, value) ->
      DetailRow(key, value)
    }
  }

  Spacer(modifier = LivewireModifier.padding(vertical = 8.dp))

  event.response?.let { response ->
    Text(
      text = "Response Headers",
      style = TextStyle.TitleSmall,
      modifier = LivewireModifier.padding(bottom = 8.dp),
    )
    if (response.headers.isEmpty()) {
      Text(
        text = "No headers",
        style = TextStyle.BodySmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
      )
    } else {
      response.headers.forEach { (key, value) ->
        DetailRow(key, value)
      }
    }
  }
}

@Composable
private fun BodyTab(event: NetworkEvent) {
  Text(
    text = "Request Body",
    style = TextStyle.TitleSmall,
    modifier = LivewireModifier.padding(bottom = 8.dp),
  )
  Text(
    text = event.request.body ?: "(empty)",
    style = TextStyle.BodySmall,
    color = if (event.request.body != null) {
      LivewireTheme.colorScheme.onSurface
    } else {
      LivewireTheme.colorScheme.onSurfaceVariant
    },
  )

  Spacer(modifier = LivewireModifier.padding(vertical = 8.dp))

  event.response?.let { response ->
    Text(
      text = "Response Body",
      style = TextStyle.TitleSmall,
      modifier = LivewireModifier.padding(bottom = 8.dp),
    )
    Text(
      text = response.body ?: "(empty)",
      style = TextStyle.BodySmall,
      color = if (response.body != null) {
        LivewireTheme.colorScheme.onSurface
      } else {
        LivewireTheme.colorScheme.onSurfaceVariant
      },
    )
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = LivewireModifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
  ) {
    Text(
      text = label,
      style = TextStyle.LabelSmall,
      color = LivewireTheme.colorScheme.onSurfaceVariant,
      modifier = LivewireModifier.padding(right = 8.dp),
    )
    Text(
      text = value,
      style = TextStyle.BodySmall,
    )
  }
}
