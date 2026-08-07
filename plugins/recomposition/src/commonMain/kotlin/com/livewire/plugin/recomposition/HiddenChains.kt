package com.livewire.plugin.recomposition

internal fun List<ComposableNode>.withoutHiddenChains(ancestry: List<String>): List<ComposableNode> {
  val chains = HiddenChains.filter { ancestry.endsWithPath(it.under) }
  if (chains.isEmpty()) return this

  val hidden = filterTo(HashSet()) { node -> chains.any { node.matchesPath(it.hide) } }
  return if (hidden.isEmpty()) this else filterNot { it in hidden }
}

private fun ComposableNode.matchesPath(path: List<String>): Boolean {
  if (name != path.first()) return false
  val rest = path.drop(1)
  return rest.isEmpty() || children.any { it.matchesPath(rest) }
}

private fun List<String>.endsWithPath(suffix: List<String>): Boolean {
  if (suffix.size > size) return false
  val offset = size - suffix.size
  return suffix.indices.all { this[offset + it] == suffix[it] }
}

internal class HiddenChain(under: String, hide: String) {
  val under: List<String> = under.split(ChainSeparator)
  val hide: List<String> = hide.split(ChainSeparator)
}

// jetbrains hot reload injects this chain into every composition and its trailing Spacer recomposes constantly, just hide it
private val HiddenChains = listOf(
  HiddenChain(
    under = "ReloadEffects > OverlayLayout > Layout",
    hide = "EffectVisibility > Box > Layout > Spacer",
  ),
)

private const val ChainSeparator = " > "
