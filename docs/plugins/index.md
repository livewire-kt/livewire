# Plugins

Everything you see in the host is a **plugin**. A plugin is a small piece of tooling that runs *inside your app* — with full access to its runtime, databases, and HTTP clients — and describes its UI with Livewire's Compose-based widget set. That UI is streamed to the host and rendered there; nothing is drawn on the device.

## The `Plugin` interface

A plugin is two things: an identity, and a composable.

```kotlin
interface Plugin {
  /** How this plugin appears in the host application */
  val info: PluginInfo

  /** Custom composition to build UI to be remotely rendered */
  @LivewireComposable
  @Composable
  fun Content()
}
```

`PluginInfo` carries the identity shown in the host's plugin list:

```kotlin
PluginInfo(
  pluginId = "database",     // stable, unique id
  title = "Database",        // display name in the host
  icon = Icons.Rounded.Storage, // optional — any Compose ImageVector
)
```

## Installing plugins

Plugins are registered on the `LivewireClient` builder:

```kotlin
val livewireClient = LivewireClient {
  install(DatabasePlugin(context))
  install(NetworkPlugin())
  install(RecompositionPlugin())
}
```

When the host connects, the client sends a manifest enumerating every installed plugin — the host lists them by `title` and `icon`, no host-side registration needed.

## Lifecycle

There are no explicit lifecycle hooks — a plugin's lifecycle *is* its composition:

1. When you select a plugin in the host, the client launches `Content()` in a headless composition (powered by [Molecule](https://github.com/cashapp/molecule)) wrapped in the client's `LivewireTheme`.
2. Every recomposition emits an updated `LayoutNode` tree, which is diffed and streamed to the host as a full tree or a patch list.
3. Interactions on the host (clicks, text input, slider drags) come back as `LivewireAction` messages and fire the corresponding callbacks in your composition.
4. When the plugin is deselected or the connection drops, the composition is disposed.

Because it's a real composition, ordinary Compose idioms just work: `remember`, `mutableStateOf`, `collectAsState()`, `LaunchedEffect`, and `DisposableEffect` for setup/teardown that should follow the plugin's visibility.

!!! info "Crash isolation"
    If `Content()` throws, the client reports `PluginCrashed` to the host and clears the active plugin — your app keeps running, and the host shows the failure instead of the plugin panel.

## What's next

- Browse the [existing plugins](existing.md) that ship with Livewire
- Write your own — see [Building Plugins](building.md)
