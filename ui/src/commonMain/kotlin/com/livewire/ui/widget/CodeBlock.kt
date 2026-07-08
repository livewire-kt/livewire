package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

/**
 * Displays structured text content (JSON, XML, etc.) with formatting and
 * syntax highlighting applied by the host renderer. Content scrolls
 * internally, so give this widget a bounded size (e.g. `weight(1f)`).
 *
 * @param language The content language, or null to let the host detect it
 * from the content itself.
 * @param searchable Shows a host-rendered search bar above the content.
 * Search runs entirely on the host — the query never round-trips to the
 * guest. Currently supported for JSON content only.
 */
@LivewireComposable
@Composable
fun CodeBlock(
  content: String,
  modifier: LivewireModifier = LivewireModifier,
  language: CodeLanguage? = null,
  searchable: Boolean = false,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<CodeBlockNode, Applier<LayoutNode>>(
    factory = { CodeBlockNode(content) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(content, CodeBlockNode.SetContent)
      set(language, CodeBlockNode.SetLanguage)
      set(searchable, CodeBlockNode.SetSearchable)
    },
  )
}

@LivewireSerializer
@Serializable
class CodeBlockNode(
  var content: String,
  var language: CodeLanguage? = null,
  var searchable: Boolean = false,
) : LayoutNode() {

  companion object {
    val SetContent: CodeBlockNode.(String) -> Unit = applier { content = it }
    val SetLanguage: CodeBlockNode.(CodeLanguage?) -> Unit = applier { language = it }
    val SetSearchable: CodeBlockNode.(Boolean) -> Unit = applier { searchable = it }
  }
}

enum class CodeLanguage {
  Json,
  Xml,
  Html,
  PlainText;

  companion object {
    /**
     * Maps a MIME content type (e.g. "application/json; charset=utf-8")
     * to a language, or null if the type is unknown.
     */
    fun fromContentType(contentType: String?): CodeLanguage? = when {
      contentType == null -> null
      contentType.contains("json") -> Json
      contentType.contains("html") -> Html
      contentType.contains("xml") -> Xml
      contentType.startsWith("text/") -> PlainText
      else -> null
    }
  }
}
