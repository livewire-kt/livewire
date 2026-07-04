@file:OptIn(ComposeToolingApi::class)

package com.livewire.plugin.recomposition

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionGroup

internal class FakeScope : RecomposeScope {
  override fun invalidate() = Unit
}

internal class FakeGroup(
  override val key: Any,
  override val sourceInfo: String?,
  override val data: Iterable<Any?> = emptyList(),
  override val identity: Any? = null,
  override val compositionGroups: Iterable<CompositionGroup> = emptyList(),
) : CompositionGroup {
  override val node: Any? = null
  override val isEmpty: Boolean get() = compositionGroups.none()
}

internal fun composable(
  name: String,
  scope: RecomposeScope? = FakeScope(),
  identity: Any = name,
  children: List<CompositionGroup> = emptyList(),
): FakeGroup = FakeGroup(
  key = identity,
  sourceInfo = "C($name)",
  data = listOfNotNull(scope),
  identity = identity,
  compositionGroups = children,
)

internal fun transparent(
  identity: Any,
  scope: RecomposeScope? = null,
  children: List<CompositionGroup> = emptyList(),
): FakeGroup = FakeGroup(
  key = identity,
  sourceInfo = null,
  data = listOfNotNull(scope),
  identity = identity,
  compositionGroups = children,
)

internal fun ComposableNode.find(name: String): ComposableNode? =
  if (this.name == name) this else children.firstNotNullOfOrNull { it.find(name) }

internal fun List<ComposableNode>.find(name: String): ComposableNode = firstNotNullOfOrNull { it.find(name) } ?: error("no node named $name")
