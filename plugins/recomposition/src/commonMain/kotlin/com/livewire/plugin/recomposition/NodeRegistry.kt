package com.livewire.plugin.recomposition

import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.RecomposeScope

internal class NodeRegistry {
  private val identityNodes = HashMap<Any, ComposableNode>()
  private val scopeNodes = HashMap<RecomposeScope, ComposableNode>()
  private val contextGraftPoints = HashMap<CompositionContext, ComposableNode>()

  fun obtain(identity: Any, name: String): ComposableNode = identityNodes.getOrPut(identity) { ComposableNode(key = identity, name = name) }

  fun bindScope(scope: RecomposeScope, node: ComposableNode) {
    scopeNodes[scope] = node
  }

  fun unbindScope(scope: RecomposeScope) = scopeNodes.remove(scope)

  fun nodeForScope(scope: RecomposeScope): ComposableNode? = scopeNodes[scope]

  fun recordGraftPoint(context: CompositionContext, node: ComposableNode) {
    contextGraftPoints[context] = node
  }

  fun graftPointFor(context: CompositionContext): ComposableNode? = contextGraftPoints[context]

  fun prune(liveRoots: List<ComposableNode>) {
    val liveKeys = HashSet<Any>()
    for (root in liveRoots) aggregateLiveKeys(root, liveKeys)

    identityNodes.keys.retainAll(liveKeys)
    contextGraftPoints.entries.retainAll { it.value.key in liveKeys }
  }

  fun clear() {
    identityNodes.clear()
    scopeNodes.clear()
    contextGraftPoints.clear()
  }

  private fun aggregateLiveKeys(node: ComposableNode, aggregator: MutableSet<Any>) {
    aggregator.add(node.key)
    for (child in node.children) aggregateLiveKeys(child, aggregator)
  }

  // TODO: where's our visiblefortesting?
  internal fun trackedIdentities(): Set<Any> = identityNodes.keys.toSet()
}
