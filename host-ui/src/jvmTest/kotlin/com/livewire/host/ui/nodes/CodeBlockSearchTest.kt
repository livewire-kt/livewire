package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.AnnotatedString
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.CodeLanguage
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing

@OptIn(ExperimentalComposeUiApi::class)
class CodeBlockSearchTest {
  @Test
  fun textSearchSurvivesEachKeystrokeAndTreeReplacement() = runBlocking(Dispatchers.Swing) {
    val colors = lightColorScheme()
    val payload = mutableStateOf("""{"user":"Alice"}""")
    val scene = ImageComposeScene(600, 400, coroutineContext = coroutineContext) {
      CompositionLocalProvider(LocalUriHandler provides object : UriHandler {
        override fun openUri(uri: String) = Unit
      }) {
        MaterialTheme(colorScheme = colors) {
          CodeBlockNodeContent(
            CodeBlockNode(payload.value, language = CodeLanguage.Json, searchable = true),
            Modifier.fillMaxSize(),
          )
        }
      }
    }
    try {
      fun nodes(): List<SemanticsNode> {
        fun descendants(node: SemanticsNode): List<SemanticsNode> =
          listOf(node) + node.children.flatMap(::descendants)
        return scene.semanticsOwners.flatMap { descendants(it.unmergedRootSemanticsNode) }
      }
      fun texts() = nodes().flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }
      suspend fun await(description: String, condition: () -> Boolean) {
        repeat(180) {
          scene.render().close()
          if (condition()) return
          delay(16)
        }
        error("Timed out: $description; texts=${texts()}")
      }
      suspend fun enter(query: String) {
        val field = nodes().single { it.config.getOrNull(SemanticsActions.SetText) != null }
        assertTrue(field.config[SemanticsActions.SetText].action!!(AnnotatedString(query)))
      }
      suspend fun expectMatches(query: String, counter: String) {
        await("matches for '$query'") {
          texts().any { it.text == counter } && texts().any { text ->
            text.spanStyles.any { it.item.background == colors.primaryContainer }
          }
        }
        // A later reset must not erase a successful result on the next frames.
        repeat(6) {
          delay(16)
          scene.render().close()
          assertTrue(texts().any { it.text == counter }, "Lost matches for '$query': ${texts()}")
        }
      }
      await("initial JSON") { texts().any { "Alice" in it.text } }
      for (query in listOf("A", "Al", "Ali", "Alic", "Alice", "Alic", "Ali", "Al", "A")) {
        enter(query)
        expectMatches(query, "1 / 1")
      }
      enter("$.user")
      await("JSONPath result") { texts().any { "Alice" in it.text } && texts().none { it.text == "1 / 1" } }
      enter("Alice")
      expectMatches("Alice", "1 / 1")
      payload.value = """{"users":["Alice","Alice"]}"""
      expectMatches("Alice after payload replacement", "1 / 2")
      enter("")
      await("cleared highlights") {
        texts().none { text -> text.spanStyles.any { it.item.background == colors.primaryContainer } }
      }
    } finally {
      scene.close()
    }
  }
}
