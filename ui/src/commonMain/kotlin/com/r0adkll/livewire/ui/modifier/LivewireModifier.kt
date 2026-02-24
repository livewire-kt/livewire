package com.r0adkll.livewire.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Stable
sealed interface LivewireModifier {

  @Composable
  fun Modifier.toComposeUi(): Modifier = this

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

    companion object {
      internal const val BOUNDARY = "<<element>>"
    }
  }

  companion object : LivewireModifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = initial
    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R = initial
    override fun any(predicate: (Element) -> Boolean): Boolean = false
    override fun all(predicate: (Element) -> Boolean): Boolean = true
    override infix fun then(other: LivewireModifier): LivewireModifier = other
    override fun toString(): String = "Modifier"

    @Composable
    override fun Modifier.toComposeUi(): Modifier = this
  }
}

@Composable
@Suppress("ModifierFactoryExtensionFunction")
fun LivewireModifier.toComposeUi(): Modifier {
  return Modifier.toComposeUi()
}

@Composable
@Suppress("ModifierFactoryExtensionFunction")
fun LivewireModifier.toComposeUiLayout(): Modifier {
  return this.foldIn<LivewireModifier>(LivewireModifier) { r, n -> r.then(n) }.toComposeUi()
}

/**
 * A node in a [LivewireModifier] chain. A CombinedModifier always contains at least two elements; a
 * Modifier [outer] that wraps around the Modifier [inner].
 */
@Serializable
internal class CombinedLivewireModifier(
  private val outer: LivewireModifier,
  private val inner: LivewireModifier,
) : LivewireModifier {

  @Composable
  override fun Modifier.toComposeUi(): Modifier {
    var result = this

    result = with(outer) { result.toComposeUi() }
    result = with(inner) { result.toComposeUi() }

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
