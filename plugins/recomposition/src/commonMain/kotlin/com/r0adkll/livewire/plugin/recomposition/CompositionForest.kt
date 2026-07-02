package com.r0adkll.livewire.plugin.recomposition

internal class GraftResult(
  val graftPoints: Set<ComposableNode>,
  val orphans: List<ComposableNode>,
)

internal fun <T : Any> graftSubcompositions(
  subCompositions: List<Pair<T, List<ComposableNode>>>,
  previousGraftPoints: Set<ComposableNode>,
  graftPointFor: (T) -> ComposableNode?,
): GraftResult {
  val assignments = LinkedHashMap<ComposableNode, MutableList<ComposableNode>>()
  val orphans = mutableListOf<ComposableNode>()

  for ((parentContext, roots) in subCompositions) {
    val graftPoint = graftPointFor(parentContext)
    if (graftPoint != null) {
      assignments.getOrPut(graftPoint) { mutableListOf() }.addAll(roots)
    } else {
      orphans.addAll(roots)
    }
  }

  for (node in previousGraftPoints - assignments.keys) {
    node.setGraftedChildren(emptyList())
  }
  for ((node, roots) in assignments) {
    node.setGraftedChildren(roots)
  }

  return GraftResult(assignments.keys, orphans)
}

internal fun List<ComposableNode>.isDesktopWindowPlumbing(): Boolean = isEmpty() || all { it.name in WindowPlumbingNodes }

internal fun <T : Any> groupRootCompositions(
  rootsByGroup: Map<T, List<ComposableNode>>,
  orphans: List<ComposableNode>,
  groupNode: (T, List<ComposableNode>) -> ComposableNode,
): List<ComposableNode> {
  val grouped = if (rootsByGroup.size <= 1) {
    rootsByGroup.values.flatten()
  } else {
    rootsByGroup.map { (group, roots) -> groupNode(group, roots) }
  }
  return grouped + orphans
}

private val WindowPlumbingNodes = setOf("Window", "DialogWindow", "Tray")
