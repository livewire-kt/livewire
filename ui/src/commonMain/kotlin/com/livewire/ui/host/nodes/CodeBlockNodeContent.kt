package com.livewire.ui.host.nodes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
  onError: (Throwable) -> Unit,
  modifier: Modifier = Modifier,
) {
  JsonTree(
    json = json,
    onLoading = {
      Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    },
    colors = MaterialTheme.colorScheme.asTreeColors,
    initialState = TreeState.FIRST_ITEM_EXPANDED,
    textStyle = codeTextStyle(),
    showItemCount = true,
    onError = onError,
    modifier = modifier,
  )
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
