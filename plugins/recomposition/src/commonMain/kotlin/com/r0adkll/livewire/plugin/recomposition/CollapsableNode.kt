package com.r0adkll.livewire.plugin.recomposition

internal data class CollapsableNode(
  val name: String,
  val breadcrumbs: List<BreadcrumbEntry>,
  val recompositionCount: Int,
  val skipCount: Int,
  val childRecompositionCount: Int,
  val children: List<CollapsableNode>,
  val key: Any,
  val invalidationReasons: List<InvalidationReason>,
  val parameters: List<ParameterInfo>,
  val recompositionRate: Float,
)

internal data class BreadcrumbEntry(
  val name: String,
  val key: Any,
  val parameters: List<ParameterInfo>,
)

internal fun collapse(roots: List<ComposableNode>): List<CollapsableNode> {
  return roots.flatMap { collapseNode(it, inheritedReasons = emptyList(), isTreeRoot = true) }
    .map { insertChildRecompositionCounts(it) }
}

private fun insertChildRecompositionCounts(node: CollapsableNode): CollapsableNode {
  val children = node.children.map { insertChildRecompositionCounts(it) }
  return node.copy(
    children = children,
    childRecompositionCount = children.sumOf { it.recompositionCount + it.childRecompositionCount },
  )
}

private fun collapseNode(
  node: ComposableNode,
  inheritedReasons: List<InvalidationReason>,
  isTreeRoot: Boolean = false,
): List<CollapsableNode> {
  val nodeReasons = node.recentInvalidationReasons()

  // hoist children of uninteresting containers to keep hierarchy readout manageable
  if (node.name in UninterestingContainers) {
    val propagated = inheritedReasons + nodeReasons
    return node.children.toList().flatMap {
      collapseNode(it, inheritedReasons = propagated, isTreeRoot = isTreeRoot)
    }
  }

  // pick the best invalidation reasons to display
  val applicableInherited = if (node.recompositionCount > 0) inheritedReasons else emptyList()
  val hasOwnKnownReasons = nodeReasons.any { it !is InvalidationReason.Unknown }
  val combinedReasons = when {
    hasOwnKnownReasons -> nodeReasons
    applicableInherited.isNotEmpty() -> applicableInherited
    else -> nodeReasons
  }.takeLast(RecentReasonsCount)

  // collapse children as possible
  val children = node.children.toList().flatMap {
    collapseNode(it, inheritedReasons = combinedReasons)
  }

  // collapse single-child chains into breadcrumb chips
  return listOf(collapseBreadcrumbs(node, children, combinedReasons, preserveRoot = isTreeRoot))
}

private fun collapseBreadcrumbs(
  node: ComposableNode,
  children: List<CollapsableNode>,
  combinedReasons: List<InvalidationReason>,
  preserveRoot: Boolean = false,
): CollapsableNode {
  var current = CollapsableNode(
    name = node.name,
    breadcrumbs = emptyList(),
    recompositionCount = node.recompositionCount,
    skipCount = node.skipCount,
    childRecompositionCount = 0,
    children = children,
    key = node.key,
    invalidationReasons = combinedReasons,
    parameters = node.parameters,
    recompositionRate = node.recompositionRate(),
  )

  if (preserveRoot && current.children.size == 1) {
    current = collapseTransparentPairs(current)
    if (current.children.size == 1) {
      current = current.copy(children = listOf(collapseChain(current.children[0])))
    }
    return current
  }

  return collapseChain(current)
}

private fun collapseChain(node: CollapsableNode): CollapsableNode {
  val breadcrumbs = mutableListOf<BreadcrumbEntry>()
  var current = node

  while (current.children.size == 1) {
    val collapsed = collapseTransparentPair(current, current.children[0])
    if (collapsed != null) {
      current = collapsed
    } else {
      breadcrumbs.add(BreadcrumbEntry(name = current.name, key = current.key, parameters = current.parameters))
      current = current.children[0]
    }
  }

  return current.copy(breadcrumbs = breadcrumbs + current.breadcrumbs)
}

private fun collapseTransparentPairs(node: CollapsableNode): CollapsableNode {
  var current = node
  while (current.children.size == 1) {
    val collapsed = collapseTransparentPair(current, current.children[0]) ?: break
    current = collapsed
  }
  return current
}

private fun collapseTransparentPair(parent: CollapsableNode, child: CollapsableNode): CollapsableNode? = when {
  // absorb Layout children (ex Box > Layout > Box)
  child.name == "Layout" -> parent.copy(children = child.children)
  // promote through Layout parents — replace with child (ex Layout > Scaffold > Surface)
  parent.name == "Layout" -> child
  // absorb same-name children (ex MaterialTheme > MaterialTheme)
  parent.name == child.name -> parent.copy(children = child.children)
  else -> null
}

// children of these are hoisted to their parent
private val UninterestingContainers = setOf(
  "ReusableComposeNode",
  "BackgroundTextMeasurement",
  "SubcomposeLayout",
  "LazyLayout",
  "LazySaveableStateHolderProvider",
  "LazyLayoutPinnableItem",
  "SaveableStateProvider",
  "NavigationBarItemLayout",
  "Item",
  "BasicText",
  "ReusableContentHost",
  "TrackInteropPlacementContainer",
  "SwingWindow",
  "AwtWindow",
  "WindowContentContainer",
  "WindowContentLayout",
  "OverlayLayout",
  "OffsetToFocusedRect",
  "FocusAboveKeyboardIfNeeded",
)

private const val RecentReasonsCount = 3
