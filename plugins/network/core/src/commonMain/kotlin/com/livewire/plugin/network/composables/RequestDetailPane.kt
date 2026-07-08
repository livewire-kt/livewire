package com.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.livewire.plugin.network.DetailSection
import com.livewire.plugin.network.data.NetworkEvent
import com.livewire.plugin.network.data.NetworkResponse
import com.livewire.plugin.network.ui.ChevronRight
import com.livewire.plugin.network.ui.Close
import com.livewire.plugin.network.ui.Icons
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.ColumnScope
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.clickable
import com.livewire.ui.modifier.copyClickable
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.rotate
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.thenIf
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.util.asReadableBytes
import com.livewire.ui.widget.CodeBlock
import com.livewire.ui.widget.CodeLanguage
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.Image
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextStyle

@Composable
internal fun RequestDetailPane(
  event: NetworkEvent,
  expandedSections: Set<DetailSection>,
  onToggleSection: (DetailSection) -> Unit,
  onClose: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
  ) {
    // Header bar
    Surface(
      modifier = LivewireModifier.height(60.dp),
      shadowElevation = 4.dp,
    ) {
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
          Icon(imageVector = Icons.Close)
        }
      }
    }

    // Sectioned detail list. Body sections host viewers that scroll
    // internally, so this column is not scrollable itself — expanded
    // body sections split the remaining space via weights.
    Column(
      modifier = LivewireModifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
    ) {
      Overview(event)

      HeadersSection(
        title = "Request Headers",
        section = DetailSection.RequestHeaders,
        headers = event.request.headers,
        expanded = DetailSection.RequestHeaders in expandedSections,
        onToggleSection = onToggleSection,
      )

      event.response?.let { response ->
        HeadersSection(
          title = "Response Headers",
          section = DetailSection.ResponseHeaders,
          headers = response.headers,
          expanded = DetailSection.ResponseHeaders in expandedSections,
          onToggleSection = onToggleSection,
        )
      }

      RequestBodySection(
        event = event,
        expanded = DetailSection.RequestBody in expandedSections,
        onToggleSection = onToggleSection,
      )

      event.response?.let { response ->
        ResponseBodySection(
          response = response,
          expanded = DetailSection.ResponseBody in expandedSections,
          onToggleSection = onToggleSection,
        )
      }

      Spacer(LivewireModifier.height(8.dp))
    }
  }
}

@Composable
private fun Overview(event: NetworkEvent) {
  Column(
    modifier = LivewireModifier.padding(vertical = 8.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = LivewireTheme.colorScheme.secondaryContainer,
      modifier = LivewireModifier
        .fillMaxWidth()
        .copyClickable(event.request.url),
    ) {
      Text(
        text = event.request.url,
        style = TextStyle.LabelLarge,
        modifier = LivewireModifier.padding(
          horizontal = 12.dp,
          vertical = 8.dp,
        ),
      )
    }

    Spacer(LivewireModifier.height(4.dp))

    Row {
      OverviewChip(
        text = event.request.method.uppercase(),
        color = LivewireTheme.colorScheme.primaryContainer,
      )

      event.response?.let { response ->
        Spacer(LivewireModifier.width(4.dp))
        OverviewChip(
          text = response.statusCode.toString(),
          color = LivewireTheme.colorScheme.tertiaryContainer,
        )
      }

      event.response?.contentLength?.let { contentLength ->
        Spacer(LivewireModifier.width(4.dp))
        OverviewChip(
          text = contentLength.asReadableBytes(),
          color = LivewireTheme.colorScheme.tertiaryContainer,
        )
      }
    }
  }
}

@Composable
private fun OverviewChip(
  text: String,
  color: Color = LivewireTheme.colorScheme.primaryContainer,
  modifier: LivewireModifier = LivewireModifier,
  onClickAction: ClickAction? = null
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = color,
    modifier = modifier,
    onClick = onClickAction,
  ) {
    Text(
      text = text,
      style = TextStyle.LabelSmall,
      fontWeight = FontWeight.Bold.weight,
      modifier = LivewireModifier.padding(
        horizontal = 16.dp,
        vertical = 8.dp,
      ),
    )
  }
}

@Composable
private fun HeadersSection(
  title: String,
  section: DetailSection,
  headers: Map<String, String>,
  expanded: Boolean,
  onToggleSection: (DetailSection) -> Unit,
) {
  SectionHeader(
    title = title,
    section = section,
    expanded = expanded,
    onToggleSection = onToggleSection,
    meta = if (headers.isEmpty()) "none" else "${headers.size}",
  )
  if (expanded) {
    if (headers.isEmpty()) {
      EmptyValue("No headers")
    } else {
      headers.forEach { (key, value) ->
        DetailRow(key, value)
      }
    }
  }
}

@Composable
private fun ColumnScope.RequestBodySection(
  event: NetworkEvent,
  expanded: Boolean,
  onToggleSection: (DetailSection) -> Unit,
) {
  val body = event.request.body
  SectionHeader(
    title = "Request Body",
    section = DetailSection.RequestBody,
    expanded = expanded,
    onToggleSection = onToggleSection,
    meta = event.request.contentType?.substringBefore(';') ?: if (body == null) "empty" else null,
    copyValue = body,
  )
  if (expanded) {
    if (body != null) {
      CodeBlock(
        content = body,
        language = CodeLanguage.fromContentType(event.request.contentType),
        searchable = true,
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxWidth(),
      )
    } else {
      EmptyValue("(empty)")
    }
  }
}

@Composable
private fun ColumnScope.ResponseBodySection(
  response: NetworkResponse,
  expanded: Boolean,
  onToggleSection: (DetailSection) -> Unit,
) {
  SectionHeader(
    title = "Response Body",
    section = DetailSection.ResponseBody,
    expanded = expanded,
    onToggleSection = onToggleSection,
    meta = response.contentType?.substringBefore(';')
      ?: if (response.body == null && response.bodyBytes == null) "empty" else null,
    copyValue = response.body,
  )
  if (expanded) {
    val isImageResponse = response.contentType?.startsWith("image/") == true
    when {
      isImageResponse && response.bodyBytes != null -> Image(
        imageData = response.bodyBytes,
        modifier = LivewireModifier
          .weight(2f)
          .fillMaxWidth(),
      )
      response.body != null -> CodeBlock(
        content = response.body,
        language = CodeLanguage.fromContentType(response.contentType),
        searchable = true,
        modifier = LivewireModifier
          .weight(2f)
          .fillMaxWidth(),
      )
      else -> EmptyValue("(empty)")
    }
  }
}

@Composable
private fun SectionHeader(
  title: String,
  section: DetailSection,
  expanded: Boolean,
  onToggleSection: (DetailSection) -> Unit,
  meta: String? = null,
  copyValue: String? = null,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = LivewireModifier
      .fillMaxWidth()
      .clickable(clickAction("toggle-${section.name}") { onToggleSection(section) })
      .padding(vertical = 6.dp),
  ) {
    Icon(
      imageVector = Icons.ChevronRight,
      tint = LivewireTheme.colorScheme.onSurfaceVariant,
      modifier = LivewireModifier
        .size(16.dp)
        .thenIf(expanded) { rotate(90f) },
    )
    Spacer(LivewireModifier.width(4.dp))
    Text(
      text = title,
      style = TextStyle.TitleSmall,
      modifier = LivewireModifier.weight(1f),
    )
    if (meta != null) {
      Text(
        text = meta,
        style = TextStyle.LabelSmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
      )
    }
    if (copyValue != null) {
      Text(
        text = "Copy",
        style = TextStyle.LabelSmall,
        color = LivewireTheme.colorScheme.primary,
        modifier = LivewireModifier
          .padding(left = 8.dp)
          .copyClickable(copyValue),
      )
    }
  }
}

@Composable
private fun EmptyValue(text: String) {
  Text(
    text = text,
    style = TextStyle.BodySmall,
    color = LivewireTheme.colorScheme.onSurfaceVariant,
    modifier = LivewireModifier.padding(left = 20.dp, bottom = 4.dp),
  )
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = LivewireModifier
      .copyClickable(value)
      .fillMaxWidth()
      .padding(left = 20.dp, top = 2.dp, bottom = 2.dp),
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
