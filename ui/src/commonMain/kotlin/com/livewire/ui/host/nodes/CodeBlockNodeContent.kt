package com.livewire.ui.host.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.CodeLanguage
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.search.SearchState
import com.sebastianneubauer.jsontree.search.rememberSearchState
import kotlinx.coroutines.launch

@Composable
internal fun CodeBlockNodeContent(
  node: CodeBlockNode,
  modifier: Modifier = Modifier,
) {
  val language = node.language ?: detectLanguage(node.content)
  var jsonParseFailed by remember(node.content) { mutableStateOf(false) }

  when {
    language == CodeLanguage.Json && !jsonParseFailed -> JsonContent(
      json = node.content,
      searchable = node.searchable,
      onError = { jsonParseFailed = true },
      modifier = modifier.debugFrame(),
    )
    language == CodeLanguage.Xml || language == CodeLanguage.Html -> ScrollableCode(
      text = rememberMarkupHighlight(node.content),
      modifier = modifier.debugFrame(),
    )
    else -> ScrollableCode(
      text = AnnotatedString(node.content),
      modifier = modifier.debugFrame(),
    )
  }
}

/**
 * Content-sniffing fallback for bodies served with missing or generic
 * content types (e.g. JSON as text/plain).
 */
private fun detectLanguage(content: String): CodeLanguage {
  val trimmed = content.trimStart()
  return when {
    trimmed.startsWith("{") || trimmed.startsWith("[") -> CodeLanguage.Json
    trimmed.startsWith("<") -> CodeLanguage.Xml
    else -> CodeLanguage.PlainText
  }
}

@Composable
private fun JsonContent(
  json: String,
  searchable: Boolean,
  onError: (Throwable) -> Unit,
  modifier: Modifier = Modifier,
) {
  val searchState = rememberSearchState()
  val lazyListState = rememberLazyListState()
  var query by remember { mutableStateOf("") }
  var parseGeneration by remember { mutableStateOf(0) }

  LaunchedEffect(searchState.selectedResultListIndex) {
    searchState.selectedResultListIndex?.let { lazyListState.animateScrollToItem(it) }
  }

  // A query typed while the tree is still parsing searches an empty tree
  // and is not re-run by JsonTree, so re-apply it once parsing finishes.
  // The intermediate null (plus a frame in between, so the writes don't
  // coalesce into one snapshot) forces JsonTree's search effect to restart.
  LaunchedEffect(parseGeneration) {
    if (parseGeneration > 0 && query.isNotEmpty()) {
      searchState.query = null
      withFrameNanos {}
      searchState.query = query
    }
  }

  Column(modifier) {
    if (searchable) {
      SearchRow(
        query = query,
        onQueryChange = {
          query = it
          searchState.query = it.ifEmpty { null }
        },
        searchState = searchState,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
    }
    JsonTree(
      json = json,
      onLoading = {
        DisposableEffect(Unit) {
          onDispose { parseGeneration++ }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      },
      colors = MaterialTheme.colorScheme.asTreeColors,
      initialState = TreeState.FIRST_ITEM_EXPANDED,
      textStyle = codeTextStyle(),
      showItemCount = true,
      searchState = searchState,
      lazyListState = lazyListState,
      onError = onError,
      modifier = Modifier.weight(1f).fillMaxWidth(),
    )
  }
}

@Composable
private fun SearchRow(
  query: String,
  onQueryChange: (String) -> Unit,
  searchState: SearchState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(6.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
      if (query.isEmpty()) {
        Text(
          text = "Search",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown &&
              event.key == Key.Enter &&
              searchState.totalResults > 0
            ) {
              scope.launch {
                if (event.isShiftPressed) searchState.selectPrevious() else searchState.selectNext()
              }
              true
            } else {
              false
            }
          },
      )
    }
    if (query.isNotEmpty()) {
      Text(
        text = if (searchState.totalResults > 0) {
          "${(searchState.selectedResultIndex ?: 0) + 1} / ${searchState.totalResults}"
        } else {
          "0 / 0"
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp),
      )
    }
    SearchArrowButton(
      icon = chevronUp,
      contentDescription = "Previous result",
      enabled = searchState.totalResults > 0,
      onClick = { scope.launch { searchState.selectPrevious() } },
    )
    SearchArrowButton(
      icon = chevronDown,
      contentDescription = "Next result",
      enabled = searchState.totalResults > 0,
      onClick = { scope.launch { searchState.selectNext() } },
    )
  }
}

@Composable
private fun SearchArrowButton(
  icon: ImageVector,
  contentDescription: String,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick,
    enabled = enabled,
    modifier = Modifier.size(28.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(18.dp),
    )
  }
}

private val chevronUp: ImageVector by lazy {
  ImageVector.Builder(
    name = "ChevronUp",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(7.41f, 15.41f)
      lineTo(12f, 10.83f)
      lineTo(16.59f, 15.41f)
      lineTo(18f, 14f)
      lineTo(12f, 8f)
      lineTo(6f, 14f)
      close()
    }
  }.build()
}

private val chevronDown: ImageVector by lazy {
  ImageVector.Builder(
    name = "ChevronDown",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(7.41f, 8.59f)
      lineTo(12f, 13.17f)
      lineTo(16.59f, 8.59f)
      lineTo(18f, 10f)
      lineTo(12f, 16f)
      lineTo(6f, 10f)
      close()
    }
  }.build()
}

@Composable
private fun ScrollableCode(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .horizontalScroll(rememberScrollState()),
  ) {
    SelectionContainer {
      Text(
        text = text,
        style = codeTextStyle(),
        modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
      )
    }
  }
}

@Composable
private fun codeTextStyle(): TextStyle =
  MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)

private val ColorScheme.asTreeColors: TreeColors
  get() = TreeColors(
    keyColor = primary,
    stringValueColor = tertiary,
    numberValueColor = secondary,
    booleanValueColor = secondary,
    nullValueColor = error,
    indexColor = onSurfaceVariant,
    symbolColor = onSurfaceVariant,
    iconColor = onSurfaceVariant,
    highlightColor = surfaceVariant,
    selectedHighlightColor = primaryContainer,
  )

// Alternation groups: 1 = comment, 2 = tag name, 3 = tag close,
// 4 = attribute name, 5 = quoted attribute value.
private val markupRegex = Regex(
  "(<!--[\\s\\S]*?-->|<!\\[CDATA\\[[\\s\\S]*?]]>|<[!?][\\s\\S]*?>)" +
    "|(</?[A-Za-z][\\w:.-]*)" +
    "|(/?>)" +
    "|([\\w:.-]+)(?=\\s*=\\s*[\"'])" +
    "|(\"[^\"]*\"|'[^']*')"
)

@Composable
private fun rememberMarkupHighlight(content: String): AnnotatedString {
  val colorScheme = MaterialTheme.colorScheme
  return remember(content, colorScheme) {
    buildAnnotatedString {
      append(content)
      for (match in markupRegex.findAll(content)) {
        val color = when {
          match.groups[1] != null -> colorScheme.onSurfaceVariant
          match.groups[2] != null || match.groups[3] != null -> colorScheme.primary
          match.groups[4] != null -> colorScheme.secondary
          else -> colorScheme.tertiary
        }
        addStyle(SpanStyle(color = color), match.range.first, match.range.last + 1)
      }
    }
  }
}
