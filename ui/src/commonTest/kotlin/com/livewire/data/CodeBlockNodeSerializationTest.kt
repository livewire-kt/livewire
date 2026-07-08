package com.livewire.data

import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.CodeLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
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
    )

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<CodeBlockNode>(bytes)

    assertEquals(node.content, decoded.content)
    assertEquals(node.language, decoded.language)
  }

  @Test
  fun codeBlockNodeRoundTripsPolymorphically() {
    LayoutNodeSerialization.entries.forEach { serialization ->
      val strategy = serialization.toStrategy()
      val node = CodeBlockNode(content = "<xml attr=\"value\"/>")

      val bytes = strategy.encodeToByteArray(node)
      val decoded = strategy.decodeFromByteArray(bytes)

      val decodedNode = assertIs<CodeBlockNode>(decoded)
      assertEquals(node.content, decodedNode.content)
      assertNull(decodedNode.language)
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
}
