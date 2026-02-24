package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.modifier.mapping.ComposeUiMapper
import kotlinx.serialization.Serializable

@Stable
sealed interface LivewireModifier : ComposeUiMapper {

  fun <R> foldIn(initial: R, operation: (R, Element) -> R): R
  fun <R> foldOut(initial: R, operation: (Element, R) -> R): R

  fun any(predicate: (Element) -> Boolean): Boolean
  fun all(predicate: (Element) -> Boolean): Boolean

  infix fun then(other: LivewireModifier): LivewireModifier =
    if (other === LivewireModifier) this else CombinedLivewireModifier(this, other)

  /** A single element contained within a [LivewireModifier] chain. */
  sealed interface Element : LivewireModifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = operation(initial, this)
    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R = operation(this, initial)
    override fun any(predicate: (Element) -> Boolean): Boolean = predicate(this)
    override fun all(predicate: (Element) -> Boolean): Boolean = predicate(this)
  }

  companion object : LivewireModifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = initial
    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R = initial
    override fun any(predicate: (Element) -> Boolean): Boolean = false
    override fun all(predicate: (Element) -> Boolean): Boolean = true
    override infix fun then(other: LivewireModifier): LivewireModifier = other
    override fun toString(): String = "Modifier"
  }
}

/**
 * A node in a [LivewireModifier] chain. A CombinedModifier always contains at least two elements; a
 * Modifier [outer] that wraps around the Modifier [inner].
 */
@Suppress("ModifierFactoryExtensionFunction")
@Serializable
internal class CombinedLivewireModifier(
  private val outer: LivewireModifier,
  private val inner: LivewireModifier,
) : LivewireModifier {

  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    var result = then
    result = outer.toComposeUi(result)
    result = inner.toComposeUi(result)
    return result
  }

  @Composable
  override fun ColumnScope.toComposeUi(then: Modifier): Modifier {
    var result = then
    result = with (outer) { this@toComposeUi.toComposeUi(result) }
    result = with (inner) { this@toComposeUi.toComposeUi(result) }
    return result
  }

  @Composable
  override fun RowScope.toComposeUi(then: Modifier): Modifier {
    var result = then
    result = with (outer) { this@toComposeUi.toComposeUi(result) }
    result = with (inner) { this@toComposeUi.toComposeUi(result) }
    return result
  }

  @Composable
  override fun BoxScope.toComposeUi(then: Modifier): Modifier {
    var result = then
    result = with (outer) { this@toComposeUi.toComposeUi(result) }
    result = with (inner) { this@toComposeUi.toComposeUi(result) }
    return result
  }

  override fun <R> foldIn(initial: R, operation: (R, LivewireModifier.Element) -> R): R =
    inner.foldIn(outer.foldIn(initial, operation), operation)

  override fun <R> foldOut(initial: R, operation: (LivewireModifier.Element, R) -> R): R =
    outer.foldOut(inner.foldOut(initial, operation), operation)

  override fun any(predicate: (LivewireModifier.Element) -> Boolean): Boolean =
    outer.any(predicate) || inner.any(predicate)

  override fun all(predicate: (LivewireModifier.Element) -> Boolean): Boolean =
    outer.all(predicate) && inner.all(predicate)

  override fun equals(other: Any?): Boolean =
    other is CombinedLivewireModifier && outer == other.outer && inner == other.inner

  override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

  override fun toString(): String =
    "[" +
      foldIn("") { acc, element ->
        if (acc.isEmpty()) element.toString() else "$acc, $element"
      } +
      "]"
}
