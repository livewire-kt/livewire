# Building Plugins

A Livewire plugin is ordinary Compose code that composes Livewire's serializable widget set instead of Material/Foundation. Your code runs entirely inside the client app, and only the resulting UI tree crosses the wire.

## Project setup

Your plugin module needs the Livewire UI library, the Compose runtime, and the Compose compiler plugin:

```kotlin title="build.gradle.kts"
plugins {
  kotlin("multiplatform") // or your platform of choice
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api("com.livewire-kt.livewire:ui:<version>")
      api(compose.runtime)
    }
  }
}
```

## Implement `Plugin`

Here's a complete, working plugin:

```kotlin
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import com.livewire.ui.actions.clickAction
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.padding
import com.livewire.ui.widget.*

class CounterPlugin : Plugin {
  override val info = PluginInfo(
    pluginId = "counter",
    title = "Counter",
    icon = Icons.Rounded.Numbers,
  )

  @Composable
  override fun Content() {
    var count by remember { mutableStateOf(0) }

    Column(
      modifier = LivewireModifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Text("Count: $count", style = TextStyle.TitleMedium)

      Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
          action = clickAction { count++ },
        ) {
          Text("Increment")
        }

        Button(
          action = clickAction { count = 0 },
          style = ButtonStyle.Outlined,
        ) {
          Text("Reset")
        }
      }
    }
  }
}
```

Register it and it appears in the host automatically:

```kotlin
LivewireClient {
  install(CounterPlugin())
}
```

## The building blocks

### Widgets and layout

Import from `com.livewire.ui` instead of Compose Material — the names and shapes mirror what you already know:

- **Layout**: `Column`, `Row`, `Box`, `Spacer`, `Surface`, `ResizableSurface`
- **Widgets**: `Text`, `Button`, `IconButton`, `TextField`, `Checkbox`, `Switch`, `Slider`, `Chip`, `Table`, `TabRow`, `DropdownMenu`, `CodeBlock`, `ProgressIndicator`, `AnimatedVisibility`, and more

See the full catalog in [Widgets & Modifiers](../reference/ui.md).

### Modifiers

`LivewireModifier` is a serializable clone of Compose's `Modifier`, with the extensions you'd expect:

```kotlin
LivewireModifier
  .fillMaxWidth()
  .padding(horizontal = 16.dp)
  .background(LivewireTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
  .verticalScroll()
```

### Actions instead of lambdas

Widget callbacks must survive a round trip to the host and back, so interactive widgets take **actions** rather than plain lambdas:

```kotlin
Button(action = clickAction { save() }) { Text("Save") }
Switch(checked = enabled, onCheckedChange = checkedChangeAction { enabled = it })
Slider(value = volume, onValueChange = floatValueChangeAction { volume = it })
TextField(initialValue = query, onValueChange = valueChangeAction { query = it })
```

The factory (`clickAction { }`, `checkedChangeAction { }`, `valueChangeAction { }`, `floatValueChangeAction { }`, `intValueChangeAction { }`) registers your lambda under a stable identifier derived from the composition. When the user interacts with the rendered widget in the host, the action is dispatched back over the wire and your lambda runs **on the device**.

### State and effects

Because `Content()` runs in a real (headless) composition, everything you know from Compose applies:

- `remember { mutableStateOf(...) }` for local state — mutations trigger recomposition and the updated tree streams to the host
- `collectAsState()` to observe flows from your app's runtime
- `LaunchedEffect` for async work while the plugin is open
- `DisposableEffect` for setup/teardown tied to the plugin being selected — this is your lifecycle hook

```kotlin
@Composable
override fun Content() {
  DisposableEffect(Unit) {
    tracker.start()
    onDispose { tracker.stop() }
  }
  val entries by tracker.entries.collectAsState()
  // ...
}
```

### Theming

Use `LivewireTheme.colorScheme` for colors — it resolves against the client app's theme (set via `theme(...)` on the builder), and automatically tracks the host's light/dark toggle:

```kotlin
Text("Warning", color = LivewireTheme.colorScheme.error)
```

## Tips

- **Look at the Playground.** [`plugins/playground`](https://github.com/livewire-kt/livewire/tree/main/plugins/playground) exercises every widget and is the best living reference.
- **Crashes are contained.** If `Content()` throws, the client reports it to the host and your app keeps running. The host will offer a Reload action if this happens.
- **Keep trees reasonable.** Every recomposition diffs and streams the tree. `Table` paginates for you, so you should prefer it over emitting thousands of rows.
