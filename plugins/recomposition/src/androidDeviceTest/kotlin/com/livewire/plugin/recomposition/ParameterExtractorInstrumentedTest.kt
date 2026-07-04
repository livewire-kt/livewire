@file:OptIn(ComposeToolingApi::class)

package com.livewire.plugin.recomposition

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.ParameterSourceInformation
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Job
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class ParameterExtractorInstrumentedTest {
  @Test
  fun extractsDeclaredParameterValuesFromRestartLambda() {
    val params = extract(
      compose { Target(42, "hi") },
      ParameterSourceInformation(0, "count"),
      ParameterSourceInformation(1, "label"),
    )
    assertNotNull(params)
    assertEquals("42", params.display("count"))
    assertEquals("\"hi\"", params.display("label"))
  }

  @Test
  fun reflectsUpdatedValuesAcrossCompositions() {
    val meta = arrayOf(ParameterSourceInformation(0, "count"), ParameterSourceInformation(1, "label"))
    val first = extract(compose { Target(1, "a") }, *meta)
    val second = extract(compose { Target(99, "b") }, *meta)
    assertEquals("1", first?.display("count"))
    assertEquals("99", second?.display("count"))
  }

  @Test
  fun extractsDefaultedParameterValue() {
    val params = extract(
      compose { TargetWithDefault(a = 5) },
      ParameterSourceInformation(0, "a"),
      ParameterSourceInformation(1, "b"),
    )
    assertNotNull(params)
    assertEquals("5", params.display("a"))
    assertEquals("\"def\"", params.display("b"))
  }

  @Test
  fun skipsExtensionReceiverField() {
    val scope = object : DemoScope {}
    val params = extract(
      compose { with(scope) { TargetWithReceiver(a = 7) } },
      ParameterSourceInformation(0, "a"),
    )
    assertNotNull(params)
    assertEquals("7", params.display("a"))
  }

  @Test
  fun decodesInlineClassParameter() {
    val params = extract(
      compose { TargetWithInlineClass(Color.Red) },
      ParameterSourceInformation(0, "color", inlineClass = Color::class.qualifiedName),
    )
    assertNotNull(params)
    assertEquals("#FF0000", params.display("color"))
  }
}

private val trigger = mutableStateOf(0)
private var capturedScope: RecomposeScope? = null

private interface DemoScope

@Composable
private fun Target(count: Int, label: String) {
  @Suppress("UNUSED_EXPRESSION")
  trigger.value
  capturedScope = currentRecomposeScope
}

@Composable
private fun TargetWithDefault(a: Int, b: String = "def") {
  @Suppress("UNUSED_EXPRESSION")
  trigger.value
  capturedScope = currentRecomposeScope
}

@Composable
private fun DemoScope.TargetWithReceiver(a: Int) {
  @Suppress("UNUSED_EXPRESSION")
  trigger.value
  capturedScope = currentRecomposeScope
}

@Composable
private fun TargetWithInlineClass(color: Color) {
  @Suppress("UNUSED_EXPRESSION")
  trigger.value
  capturedScope = currentRecomposeScope
}

private fun compose(content: @Composable () -> Unit): RecomposeScope {
  capturedScope = null
  InstrumentationRegistry.getInstrumentation().runOnMainSync {
    val applier = object : AbstractApplier<Any?>(null) {
      override fun insertTopDown(index: Int, instance: Any?) = Unit
      override fun insertBottomUp(index: Int, instance: Any?) = Unit
      override fun remove(index: Int, count: Int) = Unit
      override fun move(from: Int, to: Int, count: Int) = Unit
      override fun onClear() = Unit
    }
    Composition(applier, Recomposer(Job())).setContent(content)
  }
  return assertNotNull(capturedScope)
}

private fun extract(scope: RecomposeScope, vararg metadata: ParameterSourceInformation) =
  extractParametersFromLambda(scope, metadata.toList())

private fun List<ParameterInfo>.display(name: String) = first { it.name == name }.value.displayValue
