package com.r0adkll.livewire.layout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.r0adkll.livewire.ui.composition.LivewireOutput
import com.r0adkll.livewire.ui.composition.livewireFlow
import com.r0adkll.livewire.ui.data.LayoutNodeSerialization
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.LayoutNodePatch
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest

class DiffEndToEndTest {
  @Test
  fun testHostConvergesThroughAllTheThings() = runTest {
    val strategy = LayoutNodeSerialization.Protobuf.toStrategy()
    var count by mutableStateOf(0)
    var label by mutableStateOf("idle")
    var showCard by mutableStateOf(false)

    val outputs = mutableListOf<LivewireOutput>()
    val job = livewireFlow(strategy) {
      Column {
        Text(label)
        Column { repeat(count) { Text("req $it") } }
        if (showCard) {
          Column {
            Text("title")
            Text("subtitle")
          }
        }
      }
    }.onEach { outputs += it }.launchIn(this)

    val host = TestHost(strategy)
    val drain = {
      testScheduler.advanceUntilIdle()
      outputs.forEach { o ->
        when (o) {
          is LivewireOutput.FullTree -> host.receiveFull(strategy.encodeToByteArray(o.root))
          is LivewireOutput.Patches -> host.applyPatches(strategy.encodePatchList(o.patches))
        }
      }
      outputs.clear()
    }

    // init with empty list
    drain()

    // insert
    count = 1
    drain()

    // insert + property update in a single frame
    count = 3
    label = "loading"
    drain()

    // new subtree in a patch frame
    showCard = true
    drain()

    // remove
    count = 2
    drain()

    // property update only
    label = "done"
    drain()

    // remove the subtree
    showCard = false
    drain()

    job.cancel()

    assertEquals(0, host.desyncCount, "had an unexpected desync")

    // build a fresh tree to compare to the host's patched tree
    val expected = run {
      val captured = mutableListOf<LivewireOutput>()
      val job = livewireFlow(strategy) {
        Column {
          Text(label)
          Column { repeat(count) { Text("req $it") } }
          if (showCard) {
            Column {
              Text("title")
              Text("subtitle")
            }
          }
        }
      }.onEach { captured += it }.launchIn(this)

      testScheduler.advanceUntilIdle()
      job.cancel()
      (captured.first() as LivewireOutput.FullTree).root
    }

    assertEquals(nodeAssertionString(expected), nodeAssertionString(host.root!!))
  }

  // node ids are different, stringify important stuff
  private fun nodeAssertionString(n: LayoutNode, indent: String = ""): String = buildString {
    append(indent).append(n::class.simpleName)
    (n as? TextNode)?.let { append(" '").append(it.text).append("'") }
    append('\n')
    n.children.forEach { append(nodeAssertionString(it, "$indent  ")) }
  }
}

private class TestHost(private val strategy: LayoutNodeSerializationStrategy) {
  var root: LayoutNode? = null
  private val nodeMap = mutableMapOf<Long, LayoutNode>()
  private val parentMap = mutableMapOf<Long, LayoutNode>()
  var desyncCount = 0

  private fun register(node: LayoutNode, parent: LayoutNode) {
    nodeMap[node.nodeId] = node
    parentMap[node.nodeId] = parent
    node.children.forEach { register(it, node) }
  }

  private fun deregister(node: LayoutNode) {
    nodeMap.remove(node.nodeId)
    parentMap.remove(node.nodeId)
    node.children.forEach { deregister(it) }
  }

  fun receiveFull(bytes: ByteArray) {
    val decoded = strategy.decodeFromByteArray(bytes).also { it.makeObservable() }
    root = decoded
    nodeMap.clear()
    parentMap.clear()
    nodeMap[decoded.nodeId] = decoded
    decoded.children.forEach { register(it, decoded) }
  }

  fun applyPatches(bytes: ByteArray) {
    strategy.decodePatchList(bytes).forEach { patch ->
      val parentId = when (patch) {
        is LayoutNodePatch.InsertAt -> patch.parentNodeId
        is LayoutNodePatch.RemoveAt -> patch.parentNodeId
        is LayoutNodePatch.Move -> patch.parentNodeId
        is LayoutNodePatch.Clear -> patch.nodeId
        is LayoutNodePatch.UpdateNode -> patch.node.nodeId
      }
      if (parentId !in nodeMap) {
        desyncCount++
        return@forEach
      }

      when (patch) {
        is LayoutNodePatch.InsertAt -> nodeMap.getValue(patch.parentNodeId).let { parent ->
          patch.node.makeObservable()
          parent.insertAt(patch.index, patch.node)
          register(patch.node, parent)
        }

        is LayoutNodePatch.RemoveAt -> nodeMap.getValue(patch.parentNodeId).let { parent ->
          for (i in patch.index until patch.index + patch.count) deregister(parent.children[i])
          parent.removeAt(patch.index, patch.count)
        }

        is LayoutNodePatch.Move -> nodeMap.getValue(patch.parentNodeId).move(patch.from, patch.to, patch.count)
        is LayoutNodePatch.Clear -> nodeMap.getValue(patch.nodeId).let { n ->
          n.children.forEach { deregister(it) }
          n.removeAll()
        }

        is LayoutNodePatch.UpdateNode -> {
          val existing = nodeMap.getValue(patch.node.nodeId)
          val parent = parentMap.getValue(patch.node.nodeId)
          val index = parent.children.indexOf(existing)
          patch.node.makeObservable()
          patch.node.children.addAll(existing.children)
          parent.children[index] = patch.node
          nodeMap[patch.node.nodeId] = patch.node
          patch.node.children.forEach { parentMap[it.nodeId] = patch.node }
        }
      }
    }
  }
}
