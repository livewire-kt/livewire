package com.livewire.host.ui.nodes

import at.asitplus.jsonpath.core.JsonPathCompilerException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.Json

class CodeBlockJsonPathTest {
  private val document = Json.parseToJsonElement(
    """{"users":[{"name":"Alice","active":true},{"name":"Bob","active":false}],"value":null}""",
  )

  @Test
  fun queriesSelectValuesFromTheDocument() {
    val cases = mapOf(
      "$.users[0].name" to """["Alice"]""",
      "$.users[*].name" to """["Alice","Bob"]""",
      "$..name" to """["Alice","Bob"]""",
      "$.users[?(@.active == true)].name" to """["Alice"]""",
      "$.users[0:1].name" to """["Alice"]""",
      "$.missing" to "[]",
      "$.value" to "[null]",
    )
    cases.forEach { (query, expected) ->
      assertEquals(Json.parseToJsonElement(expected), queryJsonPath(document, query), query)
    }
    assertEquals(Json.parseToJsonElement("[$document]"), queryJsonPath(document, "$"))
  }

  @Test
  fun handlesScalarAndArrayDocuments() {
    assertEquals(
      Json.parseToJsonElement("[42]"),
      queryJsonPath(Json.parseToJsonElement("42"), "$"),
    )
    assertEquals(
      Json.parseToJsonElement("[2]"),
      queryJsonPath(Json.parseToJsonElement("[1,2]"), "$[1]"),
    )
  }

  @Test
  fun malformedQueryIsAnError() {
    assertFailsWith<JsonPathCompilerException> { queryJsonPath(document, "$.users[") }
  }
}
