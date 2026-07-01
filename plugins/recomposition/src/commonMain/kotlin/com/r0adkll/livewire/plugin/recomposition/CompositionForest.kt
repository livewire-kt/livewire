package com.r0adkll.livewire.plugin.recomposition

internal fun <T : Any> graftSubcompositions(
  subCompositions: List<Pair<T, List<ComposableNode>>>,
  graftPointFor: (T) -> ComposableNode?,
): List<ComposableNode> {
  return buildList {
    for ((parentContext, roots) in subCompositions) {
      val graftPoint = graftPointFor(parentContext)
      if (graftPoint != null) {
        for (child in roots) {
          if (child !in graftPoint.children) graftPoint.addChild(child)
        }
      } else {
        addAll(roots)
      }
    }
  }
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
