---
name: verify
description: How to verify Livewire changes by running the apps and capturing evidence (offscreen Compose renders, demo client + host).
---

# Verifying Livewire changes

## Architecture reminder

Plugin UI is remote: plugins emit serializable `LayoutNode`s on the guest
(demo app); the desktop host deserializes and renders them as real
Material3 Compose via `LayoutNodeContent` in the `:host-ui` module
(`host-ui/src/commonMain/kotlin/com/livewire/host/ui/`, package
`com.livewire.host.ui`). Verifying a widget/renderer change means
rendering the **host side**.

## Full-stack GUI (manual only)

- Guest/demo client: `./gradlew :demo:desktop:run` (window "Livewire Client",
  Rick & Morty screen fires real JSON API calls — good for the network plugin).
- Host: `./gradlew :host:run`.
- There is NO GUI automation on this machine: no cliclick, and
  `osascript`/System Events times out (no accessibility permission). Don't
  plan on clicking through the host app headlessly.

## Offscreen render harness (works headless, preferred)

Render the real host pipeline to PNGs with `ImageComposeScene`:

1. Drop a scratch `fun main()` into
   `demo/desktop/src/main/kotlin/com/livewire/` (this module sees `:ui`,
   all plugins, and `compose.desktop.currentOs` — but NOT `:host-ui`,
   which holds `LayoutNodeContent`; temporarily add
   `implementation(projects.hostUi)` to `demo/desktop/build.gradle.kts`).
2. Build the node(s) under test, round-trip them through
   `LayoutNodeSerializationStrategy.Default` (the real wire format), then
   render `LayoutNodeContent(node, ...)` inside
   `MaterialTheme(lightColorScheme()/darkColorScheme())` in an
   `ImageComposeScene(width, height, Density(2f))`.
3. Pump frames so async work (e.g. JsonTree parsing) settles:
   `while (frame < 60 && (scene.hasInvalidations() || frame < 10)) { Thread.sleep(50); scene.render(frame*100_000_000L); frame++ }`
4. `image.encodeToData(EncodedImageFormat.PNG)!!.bytes` → write file → Read
   the PNG to inspect visually.
5. Run it by temporarily pointing `mainClass` in
   `demo/desktop/build.gradle.kts` at the scratch file:
   `./gradlew :demo:desktop:run`.
6. Clean up: delete the scratch file and `git checkout demo/desktop/build.gradle.kts`
   (reverts both the `mainClass` and the temporary `:host-ui` dependency).

Scene width/height are px; content lays out at `density`, so budget
~2x the dp sum or the bottom gets clipped.

Interaction in headless scenes:
- `scene.sendPointerEvent(PointerEventType.Press/Release, Offset(px, px))`
  works for clicks/focus.
- `scene.sendKeyEvent(KeyEvent(key, type, ...))` works for shortcuts and
  Enter/arrows — needs `@file:OptIn(androidx.compose.ui.InternalComposeUiApi::class)`.
- Typing text does NOT work (no platform text-input service headless).
  Workaround: stage the text on the AWT clipboard
  (`Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection("..."), null)`),
  focus the field with a click, then send Meta+V — paste goes through the
  key mapping and inserts text fine.

## Full guest↔host pipeline harness (plugin UI changes)

To verify a plugin's remote UI end-to-end without the WebSocket, bridge
both sides in one process:

1. Populate real data stores (e.g. `NetworkEventCollector.recordRequest/
   recordResponse`) with fakes.
2. Guest side, on a `CoroutineScope(Dispatchers.Default)`:
   `livewireFlow(strategy, resyncFlow) { rememberLivewireActionController()
   → SideEffect { ref.set(it) } → CompositionLocalProvider(
   LocalLivewireActionObserver provides controller) { LivewireTheme(
   CustomLivewireTheme, false) { plugin.Content() } } }` — collect
   `LivewireOutput.FullTree`, re-encode/decode via the strategy, publish
   to a `MutableStateFlow<LayoutNode?>`.
3. Host side in an `ImageComposeScene`: collect the tree flow, render
   `LayoutNodeContent(tree)` under `MaterialTheme` + provide
   `LocalLivewireActionDispatcher` (an object delegating to the guest
   controller via the AtomicReference) and `LocalSnackDispatcher`
   (`rememberSnackbarDispatcher(SnackbarHostState())`, from
   `com.livewire.ui.snackbar` in `:ui`).
4. Interactions: `sendPointerEvent` clicks dispatch real actions to the
   guest presenter; after each click emit to `resyncFlow` to get a fresh
   FullTree (skips patch application). Capture PNGs between steps.
5. Coordinates shift as sections expand/collapse — capture first, read
   the PNG, then aim follow-up clicks.

## Quick compile matrix

```bash
./gradlew :ui:jvmMainClasses :host-ui:jvmMainClasses :plugins:network:core:jvmMainClasses :host:jvmMainClasses
./gradlew :ui:compileKotlinIosArm64 :host-ui:compileKotlinIosArm64  # commonMain must compile for iOS
./gradlew :demo:android:assembleDebug    # Android target
./gradlew :ui:jvmTest                    # serialization round-trip tests
```
