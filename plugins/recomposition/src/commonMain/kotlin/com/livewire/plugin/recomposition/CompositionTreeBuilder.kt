@file:OptIn(ComposeToolingApi::class)
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.livewire.plugin.recomposition

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composer
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
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.layout.positionInRoot

internal class CompositionTreeBuilder(private val registry: NodeRegistry) {

  // snapshots needed avoid concurrent modification exceptions when building the node tree on a bg thread
  fun snapshot(groups: Iterable<CompositionGroup>): List<GroupSnapshot>? = try {
    groups.map { it.toSnapshot() }.takeIf { it.isNotEmpty() }
  } catch (t: Throwable) {
    null
  }

  fun build(
    snapshot: List<GroupSnapshot>,
    pass: ScopePass,
  ): List<ComposableNode> = buildList {
    val ancestry = mutableListOf<String>()
    for (group in snapshot) {
      collect(
        group = group,
        pass = pass,
        collector = this,
        parentNode = null,
        parentExecutions = 0,
        ancestry = ancestry,
        freshContent = false,
      )
    }
  }

  @VisibleForTesting
  fun build(
    groups: Iterable<CompositionGroup>,
    pass: ScopePass,
  ): List<ComposableNode>? = snapshot(groups)?.let { build(it, pass) }

  // asking for identity mints a persistent anchor in the app's slot table, so only groups that become nodes get one
  private fun CompositionGroup.toSnapshot(): GroupSnapshot = GroupSnapshot(
    sourceInfo = sourceInfo,
    identity = if (parseComposableName(sourceInfo?.let { parseSourceInformation(it) }) != null) identity else null,
    key = key,
    data = data.toList(),
    bounds = (node as? LayoutInfo)?.bounds(),
    children = compositionGroups.map { it.toSnapshot() },
  )

  private fun LayoutInfo.bounds(): NodeBounds? = try {
    if (!isAttached || !isPlaced) {
      null
    } else {
      val position = coordinates.positionInRoot()
      NodeBounds(position.x, position.y, width.toFloat(), height.toFloat())
    }
  } catch (_: Throwable) {
    null
  }

  private fun collect(
    group: GroupSnapshot,
    pass: ScopePass,
    collector: MutableList<ComposableNode>,
    parentNode: ComposableNode?,
    parentExecutions: Int,
    ancestry: MutableList<String>,
    freshContent: Boolean,
  ): NodeBounds? {
    val sourceInfo = group.sourceInfo?.let { parseSourceInformation(it) }
    val name = parseComposableName(sourceInfo)
    val scope = group.data.firstOrNull { it is RecomposeScope } as? RecomposeScope
    val selfPaused = scope != null && scope in pass.paused
    // inline groups have no scope of their own and just run when their parent scope runs
    val executions = if (scope != null) pass.executed[scope] ?: 0 else parentExecutions
    val skips = if (scope != null) pass.skipped[scope] ?: 0 else 0
    val executed = executions > 0

    if (name != null) {
      val identity = group.identity ?: group.key
      val node = registry.obtain(identity, name)

      // deactivated reusable content keeps its groups, but the composer clears their remember slots and releases their recompose scopes
      node.deactivated = group.data.any { it === Composer.Empty }

      if (scope != null) registry.bindScope(scope, node)

      val previousParameters = node.parameters
      if (!node.deactivated) node.parameters = extractParameters(group.data, sourceInfo)

      // a lazy slot taking on a different key is composing a new item, not recomposing the old one
      val fresh = freshContent || (name == LazyItemName && executed && keyChanged(previousParameters, node.parameters))
      if (fresh) node.resetCounts(fresh = true)

      repeat(executions + skips) { node.recordEnter() }
      when {
        executed -> {
          val changed = changedArguments(previousParameters, node.parameters)
          repeat(executions) { node.recordComposition(parentExecutions > 0, if (it == executions - 1) changed else emptyList()) }
        }
        // a paused scope has been inserted but not run yet
        selfPaused -> Unit
        else -> node.markExisting()
      }

      group.data.forEach {
        unwrappedCompositionContext(it)?.let { context ->
          registry.recordGraftPoint(context, node)
        }
      }

      val childNodes = mutableListOf<ComposableNode>()
      var bounds = group.bounds
      ancestry.add(name)
      for (child in group.children) {
        val childBounds = collect(
          group = child,
          pass = pass,
          collector = childNodes,
          parentNode = node,
          parentExecutions = executions,
          ancestry = ancestry,
          freshContent = fresh,
        )
        bounds = bounds union childBounds
      }
      node.setChildren(childNodes.withoutHiddenChains(ancestry))
      ancestry.removeAt(ancestry.lastIndex)
      node.bounds = bounds

      collector.add(node)
      return bounds
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

      var bounds = group.bounds
      group.children.forEach {
        val childBounds = collect(
          group = it,
          pass = pass,
          collector = collector,
          parentNode = parentNode,
          parentExecutions = executions,
          ancestry = ancestry,
          freshContent = freshContent,
        )
        bounds = bounds union childBounds
      }
      return bounds
    }
  }

  private fun keyChanged(previous: List<ParameterInfo>, current: List<ParameterInfo>): Boolean {
    val before = previous.firstOrNull { it.name == LazyItemKeyParameter } ?: return false
    val after = current.firstOrNull { it.name == LazyItemKeyParameter } ?: return false
    return before.value != after.value
  }

  private fun changedArguments(previous: List<ParameterInfo>, current: List<ParameterInfo>): List<String> {
    if (previous.isEmpty() || previous.size != current.size) return emptyList()
    return current.mapIndexedNotNull { index, param ->
      val before = previous[index]
      if (before.name == param.name && before.value != param.value) {
        "${param.name}: ${before.value.displayValue.take(MaxArgumentPreview)} → ${param.value.displayValue.take(MaxArgumentPreview)}"
      } else {
        null
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

    // the composer only keeps slots for arguments it had to compare. the runtime lists parameters in declaration order, which is also the slot order
    val declared = metadata.sortedBy { it.sortedIndex }
    val complete = slotValues.size == declared.size
    return slotValues.take(declared.size).mapIndexed { index, value ->
      val meta = declared[index]
      ParameterInfo(
        name = if (complete) meta.name ?: "param$index" else "param$index",
        value = ParameterValue.fromValue(value, inlineClass = if (complete) meta.inlineClass else null),
      )
    }
  }
}

private const val MaxArgumentPreview = 60
private const val LazyItemName = "Item"
private const val LazyItemKeyParameter = "key"

// how often each scope ran or skipped across the passes folded into one capture
internal class ScopePass(
  val executed: Map<RecomposeScope, Int>,
  val skipped: Map<RecomposeScope, Int>,
  val paused: Set<RecomposeScope> = emptySet(),
) {
  constructor(
    executed: Set<RecomposeScope>,
    skipped: Set<RecomposeScope>,
    paused: Set<RecomposeScope> = emptySet(),
  ) : this(executed.associateWith { 1 }, skipped.associateWith { 1 }, paused)

  fun merge(other: ScopePass): ScopePass = ScopePass(
    executed = executed.sum(other.executed),
    skipped = skipped.sum(other.skipped),
    paused = paused + other.paused,
  )

  private fun Map<RecomposeScope, Int>.sum(other: Map<RecomposeScope, Int>): Map<RecomposeScope, Int> {
    if (other.isEmpty()) return this
    val result = HashMap(this)
    other.forEach { (scope, count) -> result[scope] = (result[scope] ?: 0) + count }
    return result
  }

  companion object {
    val Empty = ScopePass(emptyMap<RecomposeScope, Int>(), emptyMap())
  }
}

internal class GroupSnapshot(
  val sourceInfo: String?,
  val identity: Any?,
  val key: Any,
  val data: List<Any?>,
  val children: List<GroupSnapshot>,
  val bounds: NodeBounds? = null,
)

internal data class NodeBounds(
  val left: Float,
  val top: Float,
  val width: Float,
  val height: Float,
) {
  val right: Float get() = left + width
  val bottom: Float get() = top + height
}

internal infix fun NodeBounds?.union(other: NodeBounds?): NodeBounds? = when {
  this == null -> other
  other == null -> this
  else -> {
    val left = minOf(left, other.left)
    val top = minOf(top, other.top)
    NodeBounds(left, top, maxOf(right, other.right) - left, maxOf(bottom, other.bottom) - top)
  }
}

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
