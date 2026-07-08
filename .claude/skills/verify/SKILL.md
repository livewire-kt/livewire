---
name: verify
description: How to verify Livewire changes by running the apps and capturing evidence (offscreen Compose renders, demo client + host).
---

# Verifying Livewire changes

## Architecture reminder

Plugin UI is remote: plugins emit serializable `LayoutNode`s on the guest
(demo app); the desktop host deserializes and renders them as real
Material3 Compose via `ui/.../host/LayoutNodeContent.kt`. Verifying a
widget/renderer change means rendering the **host side**.

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
   all plugins, and `compose.desktop.currentOs`).
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
6. Clean up: delete the scratch file and `git checkout demo/desktop/build.gradle.kts`.

Scene width/height are px; content lays out at `density`, so budget
~2x the dp sum or the bottom gets clipped.

## Quick compile matrix

```bash
./gradlew :ui:jvmMainClasses :plugins:network:core:jvmMainClasses :host:jvmMainClasses
./gradlew :ui:compileKotlinIosArm64      # :ui commonMain must compile for iOS
./gradlew :demo:android:assembleDebug    # Android target
./gradlew :ui:jvmTest                    # serialization round-trip tests
```
