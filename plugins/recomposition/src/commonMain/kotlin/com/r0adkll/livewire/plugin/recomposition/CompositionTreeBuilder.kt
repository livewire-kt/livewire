@file:OptIn(ComposeToolingApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.GapComposer
import androidx.compose.runtime.LinkComposer
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.RememberObserverHolder
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.runtime.tooling.ParameterSourceInformation
import androidx.compose.runtime.tooling.SourceInformation
import androidx.compose.runtime.tooling.parseSourceInformation

internal class CompositionTreeBuilder(private val registry: NodeRegistry) {

  // snapshots needed avoid concurrent modification exceptions when building the node tree on a bg thread
  fun snapshot(groups: Iterable<CompositionGroup>): List<GroupSnapshot>? = try {
    groups.map { it.toSnapshot() }
  } catch (t: Throwable) {
    null
  }

  fun build(
    snapshot: List<GroupSnapshot>,
    composedScopes: Set<RecomposeScope>,
  ): List<ComposableNode> = buildList {
    for (group in snapshot) {
      collect(group, composedScopes, this, parentNode = null, parentExecuted = false)
    }
  }

  // TODO: where's our visiblefortests annotation?
  fun build(
    groups: Iterable<CompositionGroup>,
    composedScopes: Set<RecomposeScope>,
  ): List<ComposableNode>? = snapshot(groups)?.let { build(it, composedScopes) }

  private fun CompositionGroup.toSnapshot(): GroupSnapshot = GroupSnapshot(
    sourceInfo = sourceInfo,
    identity = identity,
    key = key,
    data = data.toList(),
    children = compositionGroups.map { it.toSnapshot() },
  )

  private fun collect(
    group: GroupSnapshot,
    composedScopes: Set<RecomposeScope>,
    collector: MutableList<ComposableNode>,
    parentNode: ComposableNode?,
    parentExecuted: Boolean,
  ) {
    val sourceInfo = group.sourceInfo?.let { parseSourceInformation(it) }
    val name = parseComposableName(sourceInfo)
    val scope = group.data.firstOrNull { it is RecomposeScope } as? RecomposeScope
    val selfExecuted = scope != null && scope in composedScopes
    val reached = parentExecuted || selfExecuted
    val childExecuted = if (scope != null) selfExecuted else parentExecuted

    if (name != null) {
      val identity = group.identity ?: group.key
      val node = registry.obtain(identity, name)

      if (scope != null) registry.bindScope(scope, node)

      if (reached) {
        node.recordEnter()
        if (scope == null || selfExecuted) {
          node.recordComposition()
        }
      }

      node.parameters = extractParameters(group.data, sourceInfo)

      group.data.forEach {
        unwrappedCompositionContext(it)?.let { context ->
          registry.recordGraftPoint(context, node)
        }
      }

      val childNodes = mutableListOf<ComposableNode>()
      for (child in group.children) {
        collect(child, composedScopes, childNodes, parentNode = node, parentExecuted = childExecuted)
      }

      node.setChildren(childNodes)

      collector.add(node)
    } else {
      // transparent groups that we'll collapse
      if (scope != null) {
        val target = parentNode ?: collector.lastOrNull()
        if (target != null) {
          registry.bindScope(scope, target)
        }
      }

      group.data.forEach {
        val context = unwrappedCompositionContext(it)
        if (context != null) {
          val graftTarget = parentNode ?: collector.lastOrNull()
          if (graftTarget != null) {
            registry.recordGraftPoint(context, graftTarget)
          }
        }
      }

      group.children.forEach {
        collect(it, composedScopes, collector, parentNode, parentExecuted = childExecuted)
      }
    }
  }

  private fun unwrappedCompositionContext(wrapper: Any?): CompositionContext? {
    if (wrapper == null) return null
    if (wrapper is CompositionContext) return wrapper

    val holder = when (wrapper) {
      is RememberObserverHolder -> wrapper.wrapped
      else -> null
    }

    return when (holder) {
      is GapComposer.CompositionContextHolder -> holder.ref
      is LinkComposer.CompositionContextHolder -> holder.ref
      is CompositionContext -> holder
      else -> null
    }
  }

  private fun extractParameters(data: List<Any?>, sourceInfo: SourceInformation?): List<ParameterInfo> {
    sourceInfo ?: return emptyList()

    val metadata = sourceInfo.parameters
      .takeIf { it.isNotEmpty() }
      ?: return emptyList()

    val scope = data.firstOrNull { it is RecomposeScope } as? RecomposeScope ?: return emptyList()

    // extract where we can (jvm), fall back to positional naming on slot data otherwise
    return extractParametersFromLambda(scope, metadata) ?: extractParametersFromSlotData(data, metadata)
  }

  private fun extractParametersFromSlotData(
    data: List<Any?>,
    metadata: List<ParameterSourceInformation>,
  ): List<ParameterInfo> {
    val scopeIndex = data.indexOfFirst { it is RecomposeScope }
    if (scopeIndex < 0) return emptyList()

    val slotValues = data.subList(scopeIndex + 1, data.size).filterNot { it is RecomposeScope }

    if (slotValues.isEmpty()) return emptyList()

    return slotValues.take(metadata.size).mapIndexed { index, value ->
      ParameterInfo(
        name = "param$index",
        value = ParameterValue.fromValue(value, inlineClass = null),
      )
    }
  }
}

internal class GroupSnapshot(
  val sourceInfo: String?,
  val identity: Any?,
  val key: Any,
  val data: List<Any?>,
  val children: List<GroupSnapshot>,
)

internal fun parseComposableName(sourceInfo: SourceInformation?): String? {
  val parsedName = sourceInfo?.functionName ?: return null

  // filter out non-Unit composables by naming convention
  if (!parsedName.first().isUpperCase()) return null

  // filter out effects
  if (parsedName.endsWith("Effect")) return null

  // filter out providers
  if (parsedName.startsWith("Provide") || parsedName.endsWith("Provider")) return null

  // filter out uninteresting internal composables
  if (parsedName == "SkippableItem" || parsedName == "ReusableComposeNode" || parsedName == "ReusableContent") return null

  return parsedName
}
