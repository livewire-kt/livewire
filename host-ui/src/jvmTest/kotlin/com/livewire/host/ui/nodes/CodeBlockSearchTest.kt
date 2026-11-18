package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing

@OptIn(ExperimentalComposeUiApi::class)
class CodeBlockSearchTest {
  @Test
  fun textSearchSurvivesEachKeystrokeAndTreeReplacement() = runBlocking(Dispatchers.Swing) {
    val literal = "${'$'}ref"
    CodeBlockScene(coroutineContext, """{"user":"Alice","id":"$literal"}""").use { block ->
      block.await("initial JSON") { hasText("Alice") }
      for (query in listOf("A", "Al", "Ali", "Alic", "Alice", "Alic", "Ali", "Al", "A")) {
        block.enter(query)
        block.expectMatches(query, "1 / 1")
      }

      block.enter("\\$literal")
      block.expectMatches(literal, "1 / 1")

      block.enter("Alice")
      block.expectMatches("Alice", "1 / 1")
      block.payload = """{"users":["Alice","Alice"]}"""
      block.expectMatches("Alice", "1 / 2")
      block.enter("")
      block.await("cleared highlights") { highlights().isEmpty() }
    }
  }

  @Test
  fun keepsLastPathResultWhileEditingAndRecoversFromAnInvalidQuery() = runBlocking(Dispatchers.Swing) {
    val json = """{"users":{"nested":{"user":"Alice"}},"excluded":"Bob"}"""
    CodeBlockScene(coroutineContext, json).use { block ->
      block.await("original JSON") { hasText("Bob") }
      block.enter("$.users")
      block.await("expanded JSONPath result") { hasText("Alice") && !hasText("Bob") }
      block.clickRow("nested")
      block.await("collapsed match") { !hasText("Alice") }
      block.clickRow("nested")
      block.await("expanded match") { hasText("Alice") }

      block.enter("$.users.nested.missing[")
      block.render()
      assertTrue(block.hasText("Alice"))
      assertFalse(block.hasText("Invalid JSONPath"))
      block.await("error with previous result still visible") {
        hasText("Invalid JSONPath. Trying to search? Escape with \\\u2060$.u…") &&
          hasText("Alice") && !hasText("Bob")
      }

      block.enter("$.excluded")
      block.await("new result") { hasText("Bob") && !hasText("Alice") && !hasText("Invalid JSONPath") }
      block.enter("Alice")
      block.expectMatches("Alice", "1 / 1")
    }
  }

  @Test
  fun largeResultsStartCollapsed() = runBlocking(Dispatchers.Swing) {
    val padding = "x".repeat(20_000)
    val json = """{"users":{"marker":"nested value","padding":"$padding"},"excluded":"original tree"}"""
    CodeBlockScene(coroutineContext, json).use { block ->
      block.await("original tree") { hasText("original tree") }
      block.enter("$.users")
      block.await("collapsed JSONPath result") {
        hasText("2 items") && !hasText("original tree") && !hasText("nested value")
      }
      block.clickRow("2 items")
      block.await("large result can be expanded") { hasText("nested value") }
    }
  }

  @Test
  fun ordinaryTreeKeepsItsScrollPositionAcrossPayloadUpdates() = runBlocking(Dispatchers.Swing) {
    fun json(label: String) = (0..99).joinToString(prefix = "[", postfix = "]") { "\"$label-$it\"" }
    CodeBlockScene(coroutineContext, json("old"), searchable = false).use { block ->
      block.await("first page") { hasText("old-0") }
      block.scrollTo(80)
      block.await("scrolled page") { hasText("old-80") && !hasText("old-0") }
      block.payload = json("new")
      block.await("updated page at the same scroll position") { hasText("new-80") && !hasText("new-0") }
    }
  }

  private class CodeBlockScene(
    context: CoroutineContext,
    initialPayload: String,
    searchable: Boolean = true,
  ) : AutoCloseable {
    var payload by mutableStateOf(initialPayload)
    private val colors = lightColorScheme()
    private val scene = ImageComposeScene(600, 400, coroutineContext = context) {
      CompositionLocalProvider(LocalUriHandler provides object : UriHandler {
        override fun openUri(uri: String) = Unit
      }) {
        MaterialTheme(colorScheme = colors) {
          CodeBlockNodeContent(
            CodeBlockNode(payload, language = CodeLanguage.Json, searchable = searchable),
            Modifier.fillMaxSize(),
          )
        }
      }
    }

    private fun descendants(node: SemanticsNode): List<SemanticsNode> =
      listOf(node) + node.children.flatMap(::descendants)

    private fun nodes() = scene.semanticsOwners.flatMap { descendants(it.unmergedRootSemanticsNode) }

    private fun SemanticsNode.texts() = config.getOrNull(SemanticsProperties.Text).orEmpty()

    private fun texts() = nodes().flatMap { it.texts() }

    fun hasText(text: String) = texts().any { text in it.text }

    fun highlights() = texts().flatMap { text ->
      text.spanStyles.filter { it.item.background == colors.primaryContainer }
        .map { text.text.substring(it.start, it.end) }
    }

    fun enter(query: String) {
      val field = nodes().single { it.config.getOrNull(SemanticsActions.SetText) != null }
      assertTrue(field.config[SemanticsActions.SetText].action!!(AnnotatedString(query)))
    }

    fun clickRow(text: String) {
      val row = nodes().first { node ->
        node.config.getOrNull(SemanticsActions.OnClick) != null &&
          descendants(node).any { child -> child.texts().any { text in it.text } }
      }
      assertTrue(row.config[SemanticsActions.OnClick].action!!())
    }

    fun scrollTo(index: Int) {
      val list = nodes().single { it.config.getOrNull(SemanticsActions.ScrollToIndex) != null }
      assertTrue(list.config[SemanticsActions.ScrollToIndex].action!!(index))
    }

    suspend fun expectMatches(query: String, counter: String) {
      fun matches() = texts().any { it.text == counter } && highlights().any { it.equals(query, ignoreCase = true) }
      await("matches for '$query'") { matches() }
      // The original bug cleared a match on a subsequent frame.
      repeat(6) {
        delay(16)
        render()
        assertTrue(matches(), "Lost matches for '$query': ${texts()}")
      }
    }

    fun render() = scene.render().close()

    suspend fun await(description: String, condition: CodeBlockScene.() -> Boolean) {
      repeat(180) {
        render()
        if (condition()) return
        delay(16)
      }
      error("Timed out: $description; texts=${texts().map { it.text.take(120) }}")
    }

    override fun close() = scene.close()
  }
}
