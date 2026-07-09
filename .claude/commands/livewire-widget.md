# Generate a Livewire Widget

Generate a new Livewire composable widget for building remote UIs. The user will describe the Material3 composable they want to wrap (e.g. "Slider", "Switch", "TextField"). Use this description along with the argument: $ARGUMENTS

## What to generate

You must create/modify exactly these files:

### 1. Widget definition: `ui/src/commonMain/kotlin/com/livewire/ui/widget/<Name>.kt`

This file contains both the client-side `@Composable` function and the serializable node class.

**Composable function pattern:**
- Annotated with `@LivewireComposable` and `@Composable`
- Uses `ReusableComposeNode<{Name}Node, Applier<LayoutNode>>` to create node instances
- Always accepts `modifier: LivewireModifier = LivewireModifier` parameter
- Before the `ReusableComposeNode` call, capture the composite key hash: `val compositeKeyHash = currentCompositeKeyHashCode.toLong()` (import `androidx.compose.runtime.currentCompositeKeyHashCode`)
- `factory` block creates the initial node
- `update` block sets all properties using companion setter references
- Always set modifier: `set(modifier, LayoutNode.SetModifier)`
- Always set compositeKeyHash: `init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)` — this enables the host to track node identity across recompositions
- If the widget has children, pass a `content` lambda and wrap it in a scope (e.g. `RowScopeInstance.content()`)

**Node class pattern:**
- Annotated with `@LivewireSerializer` (from `com.livewire.annotations.LivewireSerializer`) and `@Serializable` — the `@LivewireSerializer` annotation auto-registers the node with the KSP-generated polymorphic serializer, no manual registration needed
- Extends `LayoutNode()`
- All mutable properties are `var`
- Must override `shallowCopy(): LayoutNode` — returns a new instance of the node with the same property values but no children. Used by `deepCopy()` for fast structural copies. Pass all constructor parameters and manually copy any properties declared outside the constructor.
- Companion object contains setter lambdas using the `applier { }` helper: `val SetFoo: {Name}Node.(Type) -> Unit = applier { foo = it }`
- All properties must be serializable. For non-serializable types (like Color), convert to a serializable form (e.g. Int ARGB)

**Enums for style variants:**
- Define enums in the same file for categorical properties (e.g. style, size)
- Mark them `@Serializable`

**Actions for events:**
- If the widget needs to communicate events back to the client (e.g. onClick, onValueChange), check if an existing action type in `ui/src/commonMain/kotlin/com/livewire/ui/actions/` fits
- If no existing action fits, create a new `@Serializable` action class implementing `LivewireAction` in that directory, along with a `@Composable` factory function that wires up the observer pattern (see `ClickAction.kt` and `CheckedChangeAction.kt` for the pattern)
- New actions must also be registered as a subclass in the `LivewireAction` sealed interface's serialization

Here is an example of Text.kt for reference:
```kotlin
package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Text(
  text: String,
  modifier: LivewireModifier = LivewireModifier,
  style: TextStyle? = null,
  fontWeight: Int? = null,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TextNode, Applier<LayoutNode>>(
    factory = { TextNode(text) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(text, TextNode.SetText)
      set(style, TextNode.SetStyle)
      set(fontWeight, TextNode.SetFontWeight)
    },
  )
}

@LivewireSerializer
@Serializable
class TextNode(
  var text: String,
  var style: TextStyle? = null,
  var fontWeight: Int? = null,
) : LayoutNode() {

  companion object {
    val SetText: TextNode.(String) -> Unit = applier { text = it }
    val SetStyle: TextNode.(TextStyle?) -> Unit = applier { style = it }
    val SetFontWeight: TextNode.(Int?) -> Unit = applier { fontWeight = it }
  }
}

enum class TextStyle {
  DisplayLarge,
  DisplayMedium,
  // ...
}
```

Here is an example of Checkbox.kt for a widget with actions:
```kotlin
package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.CheckedChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Checkbox(
  checked: Boolean,
  onCheckedChange: CheckedChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<CheckboxNode, Applier<LayoutNode>>(
    factory = { CheckboxNode(checked, onCheckedChange, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(checked, CheckboxNode.SetChecked)
      set(onCheckedChange, CheckboxNode.SetCheckedChange)
      set(enabled, CheckboxNode.SetEnabled)
    }
  )
}

@LivewireSerializer
@Serializable
class CheckboxNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean,
) : LayoutNode() {

  companion object {
    val SetChecked: CheckboxNode.(Boolean) -> Unit = applier { checked = it }
    val SetCheckedChange: CheckboxNode.(CheckedChangeAction) -> Unit = applier { onCheckedChange = it }
    val SetEnabled: CheckboxNode.(Boolean) -> Unit = applier { enabled = it }
  }
}
```

### 2. Host renderer: `host-ui/src/commonMain/kotlin/com/livewire/host/ui/nodes/<Name>NodeContent.kt`

Host renderers live in the `:host-ui` module (package `com.livewire.host.ui`), NOT in `:ui` — client apps consume `:ui` and must not ship the host-side rendering code.

This renders the actual Material3 composable on the host/desktop side from the node data.

**Pattern:**
- `@Composable internal fun {Name}NodeContent(node: {Name}Node, modifier: Modifier = Modifier)`
- Always apply `modifier.debugFrame()` to the root composable
- For interactive widgets with actions, get the dispatcher: `val eventDispatcher = LocalLivewireActionDispatcher.current` and `val scope = rememberCoroutineScope()`, then dispatch in callbacks: `scope.launch { eventDispatcher.dispatch(node.action) }`
- For container widgets with children, render them: `node.children.forEach { child -> LayoutNodeContent(child, child.modifier.toComposeUi(Modifier)) }`
- Map Livewire enum values to corresponding Material3 APIs

Here is an example of CheckboxNodeContent.kt:
```kotlin
package com.livewire.host.ui.nodes

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.host.ui.debugFrame
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.widget.CheckboxNode
import kotlinx.coroutines.launch

@Composable
internal fun CheckboxNodeContent(
  node: CheckboxNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  Checkbox(
    checked = node.checked,
    onCheckedChange = {
      scope.launch {
        eventDispatcher.dispatch(node.onCheckedChange.copy(checked = it))
      }
    },
    modifier = modifier.debugFrame(),
    enabled = node.enabled,
  )
}
```

### 3. Update the router: `host-ui/src/commonMain/kotlin/com/livewire/host/ui/LayoutNodeContent.kt`

Add a new `is {Name}Node -> {Name}NodeContent(node, modifier)` branch to the `when` expression in `LayoutNodeContent()`. Add the necessary imports for the new node type and content function.

### 4. (If needed) New action type: `ui/src/commonMain/kotlin/com/livewire/ui/actions/<ActionName>.kt`

Only create if no existing action type fits. Follow the pattern from `ClickAction.kt` / `CheckedChangeAction.kt`:
- `@Immutable @Serializable data class` implementing `LivewireAction`
- Has an `identifier: String` property for routing
- Companion `@Composable` factory function that creates the action with a composition key, sets up `LaunchedEffect` to observe incoming events, and returns the action instance

When creating a new action, you must also add it as a subclass in `LivewireAction`'s serialization. Check `LivewireAction.kt` to see how the sealed interface is set up.

## Important conventions

- Package: `com.livewire.ui.widget` for widget files (`:ui` module — shared with clients)
- Package: `com.livewire.host.ui.nodes` for host renderers (`:host-ui` module — host only)
- Package: `com.livewire.ui.actions` for action types (`:ui` module)
- Naming: `{Name}Node` for node class, `{Name}NodeContent` for host renderer
- The `@LivewireSerializer` annotation auto-registers the node with the KSP-generated polymorphic serializer — no manual registration needed
- All node properties that will be transmitted must be serializable
- Use existing action types when possible (`ClickAction` for simple clicks, `CheckedChangeAction` for boolean toggles)

### 5. Add demo to PlaygroundPlugin: `plugins/playground/src/androidMain/kotlin/com/livewire/plugin/playground/PlaygroundPlugin.kt`

Add a new `Row` section inside the `Column` in `Content()` that demos the new widget with its key variants/states. Follow these conventions:

- Wrap demo widgets in a `Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)` block
- Show multiple variants to demonstrate the widget's configuration options (e.g. different styles, sizes, states)
- For widgets with actions, wire up the action factory functions (`clickAction {}`, `checkedChangeAction {}`, `valueChangeAction {}`, etc.)
- For stateful widgets, use `var state by remember { mutableStateOf(...) }` to make the demo interactive
- Use `Icons.Sync` as placeholder icon SVG data where icons are needed
- Add `modifier = LivewireModifier.padding(horizontal = 16.dp)` for spacing when appropriate
- Add any new imports needed for the widget and its enum types

Here is the current PlaygroundPlugin for reference on the existing demo structure:
```kotlin
class PlaygroundPlugin : Plugin {
  override val info: PluginInfo = PluginInfo(
    pluginId = "playground",
    iconData = Icons.Playground,
    title = "Playground",
  )

  @Composable
  override fun Content() {
    Column(LivewireModifier.fillMaxSize()) {
      // Each widget type gets its own Row section demonstrating variants
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Button style variants: Filled, Tonal, Outlined, Elevated, Text
        // IconButton style variants: Default, Filled, Tonal, Outlined
        // ToggleButton with state
      }
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Button size variants: Large, Medium, Small, ExtraSmall
      }
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Checkbox and RadioButton with shared state
      }
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // ProgressIndicator variants: Linear/Circular, determinate/indeterminate
      }
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // TextField variants: Filled and Outlined
      }
      Row(LivewireModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // FloatingActionButton variants: Small, Default, Large, Extended
      }
    }
  }
}
```

## After generating

1. Verify the code compiles: `./gradlew :ui:assemble :host-ui:assemble :plugins:playground:assemble`
2. Summarize what was created and what Material3 composable it wraps
