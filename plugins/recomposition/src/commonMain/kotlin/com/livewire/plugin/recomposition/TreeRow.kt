package com.livewire.plugin.recomposition

internal data class TreeRow(
  val node: CollapsableNode,
  val key: Any,
  val depth: Int,
  val name: String,
  val breadcrumbs: List<String>,
  val breadcrumbOriginalIndices: List<Int> = emptyList(),
  val recompositionCount: Int,
  val childRecompositionCount: Int,
  val skipCount: Int,
  val hasChildren: Boolean,
  val invalidationReasons: List<InvalidationReason>,
  val parameters: List<ParameterInfo>,
  val recompositionRate: Float = 0f,
  val isBreadcrumbRow: Boolean = false,
  val breadcrumbNodeKey: Any? = null,
  val breadcrumbIndex: Int = 0,
)

internal fun flattenTree(
  nodes: List<CollapsableNode>,
  expandOverrides: Map<Any, Boolean>,
  breadcrumbExpansions: Map<Any, Set<Int>>,
  depth: Int = 0,
  accumulator: MutableList<TreeRow> = mutableListOf(),
): List<TreeRow> {
  for (node in nodes) {
    val expanded = expandOverrides[node.key] != false
    val expandedIndices = breadcrumbExpansions[node.key].orEmpty()

    if (node.breadcrumbs.isNotEmpty() && expandedIndices.isNotEmpty()) {
      val sortedExpanded = expandedIndices.sorted()
      var depthOffset = 0
      var lastSplitEnd = 0

      for (expandedIndex in sortedExpanded) {
        val crumb = node.breadcrumbs[expandedIndex]
        val precedingChips = (lastSplitEnd until expandedIndex).map { node.breadcrumbs[it].name }
        val precedingIndices = (lastSplitEnd until expandedIndex).toList()

        accumulator.add(
          TreeRow(
            node = node,
            key = "${node.key}#crumb#$expandedIndex",
            depth = depth + depthOffset,
            name = crumb.name,
            breadcrumbs = precedingChips,
            breadcrumbOriginalIndices = precedingIndices,
            recompositionCount = 0,
            childRecompositionCount = 0,
            skipCount = 0,
            hasChildren = true,
            invalidationReasons = emptyList(),
            parameters = crumb.parameters,
            isBreadcrumbRow = true,
            breadcrumbNodeKey = node.key,
            breadcrumbIndex = expandedIndex,
          ),
        )
        depthOffset++
        lastSplitEnd = expandedIndex + 1
      }

      val remainingChips = (lastSplitEnd until node.breadcrumbs.size).map { node.breadcrumbs[it].name }
      val remainingIndices = (lastSplitEnd until node.breadcrumbs.size).toList()

      val nodeDepth = depth + depthOffset
      accumulator.add(
        TreeRow(
          node = node,
          key = node.key,
          depth = nodeDepth,
          name = node.name,
          breadcrumbs = remainingChips,
          breadcrumbOriginalIndices = remainingIndices,
          recompositionCount = node.recompositionCount,
          childRecompositionCount = node.childRecompositionCount,
          skipCount = node.skipCount,
          hasChildren = node.children.isNotEmpty(),
          invalidationReasons = node.invalidationReasons,
          parameters = node.parameters,
          recompositionRate = node.recompositionRate,
        ),
      )
      if (expanded && node.children.isNotEmpty()) {
        flattenTree(
          nodes = node.children,
          expandOverrides = expandOverrides,
          breadcrumbExpansions = breadcrumbExpansions,
          depth = nodeDepth + 1,
          accumulator = accumulator,
        )
      }
    } else {
      accumulator.add(
        TreeRow(
          node = node,
          key = node.key,
          depth = depth,
          name = node.name,
          breadcrumbs = node.breadcrumbs.map { it.name },
          breadcrumbOriginalIndices = node.breadcrumbs.indices.toList(),
          recompositionCount = node.recompositionCount,
          childRecompositionCount = node.childRecompositionCount,
          skipCount = node.skipCount,
          hasChildren = node.children.isNotEmpty(),
          invalidationReasons = node.invalidationReasons,
          parameters = node.parameters,
          recompositionRate = node.recompositionRate,
        ),
      )
      if (expanded && node.children.isNotEmpty()) {
        flattenTree(
          nodes = node.children,
          expandOverrides = expandOverrides,
          breadcrumbExpansions = breadcrumbExpansions,
          depth = depth + 1,
          accumulator = accumulator,
        )
      }
    }
  }

  return accumulator
}
