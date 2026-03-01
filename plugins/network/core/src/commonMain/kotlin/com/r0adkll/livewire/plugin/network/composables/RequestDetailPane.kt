package com.r0adkll.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
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
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.HorizontalDivider
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Tab
import com.r0adkll.livewire.ui.widget.TabRow
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle

@Composable
internal fun RequestDetailPane(
  event: NetworkEvent,
  selectedTab: Int,
  onTabSelected: IntValueChangeAction,
  onClose: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
  ) {
    // Header bar
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

    HorizontalDivider(LivewireModifier.fillMaxWidth())

    // Tab row
    TabRow(
      selectedTabIndex = selectedTab,
      onTabSelected = onTabSelected,
      modifier = LivewireModifier.fillMaxWidth(),
    ) {
      Tab(text = "Overview")
      Tab(text = "Headers")
      Tab(text = "Body")
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
  DetailRow("URL", event.request.url)
  DetailRow("Method", event.request.method)
  event.response?.let { response ->
    DetailRow("Status", response.statusCode.toString())
  }
  event.durationMs?.let { duration ->
    DetailRow("Duration", "${duration}ms")
  }
  event.request.contentType?.let { contentType ->
    DetailRow("Request Content-Type", contentType)
  }
  event.request.contentLength?.let { contentLength ->
    DetailRow("Request Content-Length", "$contentLength bytes")
  }
  event.response?.contentType?.let { contentType ->
    DetailRow("Response Content-Type", contentType)
  }
  event.response?.contentLength?.let { contentLength ->
    DetailRow("Response Content-Length", "$contentLength bytes")
  }
  event.error?.let { error ->
    DetailRow("Error", error)
  }
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
