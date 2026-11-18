package com.livewire.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.livewire.ui.composition.LivewireOutput
import com.livewire.ui.composition.livewireFlow
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.layout.LayoutNodePatch
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.widget.CodeBlock
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.CodeBlockSearchPlacement
import com.livewire.ui.widget.CodeLanguage
import com.livewire.ui.widget.CodeTreeState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
class CodeBlockNodeSerializationTest {

  @Test
  fun codeBlockNodeRoundTripsThroughProtobuf() {
    val node = CodeBlockNode(
      content = """{"user":{"name":"Alice","tags":["a","b"]}}""",
      language = CodeLanguage.Json,
      searchable = true,
      initialTreeState = CodeTreeState.Expanded,
      searchPlacement = CodeBlockSearchPlacement.Bottom,
    )

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<CodeBlockNode>(bytes)

    assertEquals(node.content, decoded.content)
    assertEquals(node.language, decoded.language)
    assertEquals(node.searchable, decoded.searchable)
    assertEquals(node.initialTreeState, decoded.initialTreeState)
    assertEquals(node.searchPlacement, decoded.searchPlacement)
  }

  @Test
  fun codeBlockNodeRoundTripsPolymorphically() {
    LayoutNodeSerialization.entries.forEach { serialization ->
      val strategy = serialization.toStrategy()
      val node = CodeBlockNode(content = "<xml attr=\"value\"/>")

      val bytes = strategy.encodeToByteArray(node)
      if (serialization == LayoutNodeSerialization.Json) {
        assertFalse(bytes.decodeToString().contains("\"searchPlacement\""))
      }
      val decoded = strategy.decodeFromByteArray(bytes)

      val decodedNode = assertIs<CodeBlockNode>(decoded)
      assertEquals(node.content, decodedNode.content)
      assertNull(decodedNode.language)
      assertEquals(CodeBlockSearchPlacement.Top, decodedNode.searchPlacement)
    }
  }

  @Test
  fun codeLanguageMapsFromContentType() {
    assertEquals(CodeLanguage.Json, CodeLanguage.fromContentType("application/json; charset=utf-8"))
    assertEquals(CodeLanguage.Xml, CodeLanguage.fromContentType("application/xml"))
    assertEquals(CodeLanguage.Xml, CodeLanguage.fromContentType("text/xml"))
    assertEquals(CodeLanguage.Html, CodeLanguage.fromContentType("text/html"))
    assertEquals(CodeLanguage.PlainText, CodeLanguage.fromContentType("text/plain"))
    assertNull(CodeLanguage.fromContentType("application/octet-stream"))
    assertNull(CodeLanguage.fromContentType(null))
  }

  @Test
  fun composableEmitsFullTreeAndPropertyUpdates() = runTest {
    LayoutNodeSerialization.entries.forEach { serialization ->
      val strategy = serialization.toStrategy()
      var content by mutableStateOf("""{"value":1}""")
      var searchable by mutableStateOf(false)
      var treeState by mutableStateOf(CodeTreeState.FirstItemExpanded)
      var placement by mutableStateOf(CodeBlockSearchPlacement.Top)
      val outputs = mutableListOf<LivewireOutput>()
      val job = livewireFlow(strategy) {
        CodeBlock(content, LivewireModifier.fillMaxWidth(), CodeLanguage.Json, searchable, treeState, placement)
      }.onEach { outputs += it }.launchIn(this)

      testScheduler.advanceUntilIdle()
      val fullTree = assertIs<LivewireOutput.FullTree>(outputs.single())
      val root = strategy.decodeFromByteArray(strategy.encodeToByteArray(fullTree.root))
      val initial = assertIs<CodeBlockNode>(root.children.single())
      assertEquals(content, initial.content)
      assertEquals(CodeLanguage.Json, initial.language)
      assertEquals(false, initial.searchable)
      assertEquals(CodeTreeState.FirstItemExpanded, initial.initialTreeState)
      assertEquals(CodeBlockSearchPlacement.Top, initial.searchPlacement)
      assertNotEquals(0L, initial.compositeKeyHash)
      assertNotEquals(LivewireModifier, initial.modifier)
      outputs.clear()

      content = """{"value":2}"""
      searchable = true
      treeState = CodeTreeState.Collapsed
      placement = CodeBlockSearchPlacement.Bottom
      testScheduler.advanceUntilIdle()
      val patches = assertIs<LivewireOutput.Patches>(outputs.single()).patches
      val patch = assertIs<LayoutNodePatch.UpdateNode>(
        strategy.decodePatchList(strategy.encodePatchList(patches)).single(),
      )
      val updated = assertIs<CodeBlockNode>(strategy.decodeFromByteArray(patch.propertyBytes))
      assertEquals(initial.nodeId, patch.nodeId)
      assertEquals(initial.nodeId, updated.nodeId)
      assertEquals(initial.compositeKeyHash, updated.compositeKeyHash)
      assertEquals(content, updated.content)
      assertEquals(CodeLanguage.Json, updated.language)
      assertEquals(true, updated.searchable)
      assertEquals(CodeTreeState.Collapsed, updated.initialTreeState)
      assertEquals(CodeBlockSearchPlacement.Bottom, updated.searchPlacement)
      job.cancel()
    }
  }
}
