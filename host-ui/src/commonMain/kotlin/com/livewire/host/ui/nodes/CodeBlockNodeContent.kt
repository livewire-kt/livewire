package com.livewire.host.ui.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import at.asitplus.jsonpath.JsonPath
import at.asitplus.jsonpath.implementation.AntlrJsonPathCompiler
import com.livewire.host.ui.debugFrame
import com.livewire.host.ui.nodes.icons.HostIcons
import com.livewire.host.ui.nodes.icons.Info
import com.livewire.logError
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.CodeBlockSearchPlacement
import com.livewire.ui.widget.CodeLanguage
import com.livewire.ui.widget.CodeTreeState
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.search.SearchState
import com.sebastianneubauer.jsontree.search.rememberSearchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

private const val JSON_PATH_DEBOUNCE_MS = 200L
private const val MAX_AUTO_EXPAND_CHARS = 16_384
private const val JSON_PATH_HELP = "https://github.com/livewire-kt/livewire/blob/main/docs/reference/ui.md#json-search-in-codeblock"

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
      searchPlacement = node.searchPlacement,
      initialState = node.initialTreeState.asTreeState,
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

private fun detectLanguage(content: String): CodeLanguage {
  val trimmed = content.trimStart()
  return when {
    trimmed.startsWith("{") || trimmed.startsWith("[") -> CodeLanguage.Json
    trimmed.startsWith("<") -> CodeLanguage.Xml
    else -> CodeLanguage.PlainText
  }
}

@OptIn(FlowPreview::class)
@Composable
private fun JsonContent(
  json: String,
  searchable: Boolean,
  searchPlacement: CodeBlockSearchPlacement,
  initialState: TreeState,
  onError: (Throwable) -> Unit,
  modifier: Modifier = Modifier,
) {
  val searchState = rememberSearchState()
  var query by remember { mutableStateOf("") }
  val pathQuery = query.takeIf { searchable && it.startsWith("$") }
  val isJsonPath = pathQuery != null
  val textListState = rememberLazyListState()
  val pathListState = key(isJsonPath) { rememberLazyListState() }
  val pathSearchState = rememberSearchState()
  // Parsing and evaluation are synchronous. Even if an effect is cancelled, let only
  // one job per CodeBlock run at a time so rapid payload changes cannot pile up work.
  val pathDispatcher = remember { Dispatchers.Default.limitedParallelism(1) }
  var document by remember(json) { mutableStateOf<JsonElement?>(null) }
  var pathResult by remember(json, isJsonPath) { mutableStateOf<String?>(null) }
  var invalidPath by remember(json, isJsonPath) { mutableStateOf<String?>(null) }
  var parseGeneration by remember { mutableStateOf(0) }

  LaunchedEffect(json, isJsonPath) {
    if (!isJsonPath) return@LaunchedEffect

    // Keep this collector running across edits. It caches the parse and processes one
    // debounced query at a time, dropping results if the input changed during evaluation.
    snapshotFlow { query }.debounce(JSON_PATH_DEBOUNCE_MS).collect { requestedQuery ->
      if (!requestedQuery.startsWith("$") || query != requestedQuery) return@collect
      val parsed = document ?: withContext(pathDispatcher) {
        runCatching { Json.parseToJsonElement(json) }
      }.getOrElse {
        onError(it)
        return@collect
      }.also { document = it }
      if (query != requestedQuery) return@collect

      // JsonTree currently takes a string, so encode only the selected values for display.
      val result = withContext(pathDispatcher) {
        runCatching { queryJsonPath(parsed, requestedQuery).toString() }
      }
      if (query == requestedQuery) {
        result.onSuccess {
          pathResult = it
          invalidPath = null
        }.onFailure {
          invalidPath = requestedQuery
        }
      }
    }
  }

  LaunchedEffect(searchState.selectedResultListIndex) {
    searchState.selectedResultListIndex?.let { textListState.animateScrollToItem(it) }
  }

  // A query typed while the tree is still parsing searches an empty tree
  // and is not re-run by JsonTree, so re-apply it once parsing finishes.
  // The intermediate null (plus a frame in between, so the writes don't
  // coalesce into one snapshot) forces JsonTree's search effect to restart.
  // Only do this after parsing: resetting on every keystroke races that effect.
  LaunchedEffect(parseGeneration) {
    if (parseGeneration > 0) {
      searchState.query = null
      withFrameNanos {}
      searchState.query = if (searchable) query.asTextQuery() else null
    }
  }

  val displayedPath = pathResult
  val pathError = pathQuery?.takeIf { it == invalidPath }?.let { invalidQuery ->
    val preview = invalidQuery.take(3)
    val suffix = if (invalidQuery.length > preview.length) "…" else ""
    "Invalid JSONPath. Trying to search? Escape with \\\u2060$preview$suffix"
  }
  val searchControls: @Composable () -> Unit = {
    SearchRow(
      query = query,
      onQueryChange = {
        query = it
        searchState.query = it.asTextQuery()
      },
      searchState = searchState,
      isJsonPath = isJsonPath,
      pathError = pathError,
      modifier = Modifier.fillMaxWidth(),
    )
    if (pathError != null) {
      Text(
        text = pathError,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
  Column(modifier) {
    if (searchable && searchPlacement == CodeBlockSearchPlacement.Top) {
      searchControls()
      Spacer(Modifier.height(4.dp))
    }
    Box(Modifier.weight(1f).fillMaxWidth()) {
      key(displayedPath != null) {
        JsonTree(
          json = displayedPath ?: json,
          onLoading = {
            if (displayedPath == null) {
              DisposableEffect(Unit) {
                onDispose { parseGeneration++ }
              }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          },
          colors = MaterialTheme.colorScheme.asTreeColors,
          initialState = when {
            displayedPath == null -> initialState
            displayedPath.length <= MAX_AUTO_EXPAND_CHARS -> TreeState.EXPANDED
            else -> TreeState.FIRST_ITEM_EXPANDED
          },
          textStyle = codeTextStyle(),
          showItemCount = true,
          searchState = if (displayedPath == null) searchState else pathSearchState,
          lazyListState = if (displayedPath == null) textListState else pathListState,
          onError = onError,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
    if (searchable && searchPlacement == CodeBlockSearchPlacement.Bottom) {
      Spacer(Modifier.height(4.dp))
      searchControls()
    }
  }
}

private fun String.asTextQuery(): String? = when {
  isEmpty() || startsWith("$") -> null
  startsWith("\\$") -> drop(1)
  else -> this
}

// The field reports invalid queries itself, so skip JSONPath4K's Napier-backed listener.
private val jsonPathCompiler = AntlrJsonPathCompiler()

internal fun queryJsonPath(document: JsonElement, query: String): JsonArray =
  JsonArray(JsonPath(query, compiler = jsonPathCompiler).query(document).map { it.value })

@Composable
private fun SearchRow(
  query: String,
  onQueryChange: (String) -> Unit,
  searchState: SearchState,
  isJsonPath: Boolean,
  pathError: String?,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val uriHandler = LocalUriHandler.current
  val colors = MaterialTheme.colorScheme
  val shape = RoundedCornerShape(6.dp)
  val textStyle = codeTextStyle().copy(color = colors.onSurface)
  var isFocused by remember { mutableStateOf(false) }
  val borderColor = when {
    pathError != null -> colors.error
    isFocused -> colors.primary
    else -> colors.outlineVariant.copy(alpha = 0.45f)
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .height(32.dp)
      .clip(shape)
      .background(colors.surfaceContainerLow)
      .border(1.dp, borderColor, shape)
      .padding(start = 8.dp, end = 2.dp),
  ) {
    BasicTextField(
      value = query,
      onValueChange = onQueryChange,
      singleLine = true,
      textStyle = textStyle,
      cursorBrush = SolidColor(colors.primary),
      modifier = Modifier
        .weight(1f)
        .onFocusChanged { isFocused = it.isFocused }
        .semantics {
          contentDescription = "Search JSON or enter JSONPath"
          if (pathError != null) error(pathError)
        }
        .onPreviewKeyEvent { event ->
          if (!isJsonPath && event.type == KeyEventType.KeyDown &&
            event.key == Key.Enter && searchState.totalResults > 0
          ) {
            scope.launch {
              if (event.isShiftPressed) searchState.selectPrevious() else searchState.selectNext()
            }
            true
          } else {
            false
          }
        },
      decorationBox = { innerTextField ->
        Box {
          if (query.isEmpty()) {
            Text("Search or JSONPath ($…)", style = textStyle, color = colors.onSurfaceVariant)
          }
          innerTextField()
        }
      },
    )
    if (!isJsonPath) {
      if (query.isNotEmpty()) {
        Text(
          text = if (searchState.totalResults > 0) {
            "${(searchState.selectedResultIndex ?: 0) + 1} / ${searchState.totalResults}"
          } else {
            "0 / 0"
          },
          style = MaterialTheme.typography.labelSmall,
          color = colors.onSurfaceVariant,
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
    IconButton(
      onClick = {
        runCatching { uriHandler.openUri(JSON_PATH_HELP) }.onFailure {
          logError("CodeBlock", "Could not open the JSONPath reference", it)
        }
      },
      modifier = Modifier.size(28.dp),
    ) {
      Icon(
        HostIcons.Info,
        contentDescription = "Open JSONPath reference",
        tint = colors.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }
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

private val CodeTreeState.asTreeState: TreeState
  get() = when (this) {
    CodeTreeState.Collapsed -> TreeState.COLLAPSED
    CodeTreeState.FirstItemExpanded -> TreeState.FIRST_ITEM_EXPANDED
    CodeTreeState.Expanded -> TreeState.EXPANDED
  }

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
