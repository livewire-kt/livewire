# Getting Started

Livewire has two halves: a **client** library you add to your app, and a **host** app you run on your desktop. This guide walks through both.

!!! warning "Work in progress"
    Published artifacts and host app downloads are not available yet. Until then, dependency coordinates are placeholders and the host runs from source.

## 1. Add the client to your app

Add the client library to your app's dependencies:

```kotlin title="build.gradle.kts"
dependencies {
  // Core SDK for integrating into your app
  implementation("com.livewire-kt.livewire:client:<version>") // Coming soon

  // SQLite Database Viewer
  implementation("com.livewire-kt.livewire:plugin-database:<version>")

  // Network Viewer
  implementation("com.livewire-kt.livewire:plugin-network-core:<version>")
  implementation("com.livewire-kt.livewire:plugin-network-ktor:<version>")
  implementation("com.livewire-kt.livewire:plugin-network-okhttp:<version>")

  // Preferences Viewer (SharedPreferences, DataStore, NSUserDefaults)
  implementation("com.livewire-kt.livewire:plugin-preferences:<version>")

  // Jetpack Compose Recomposition Viewer
  implementation("com.livewire-kt.livewire:plugin-recomposition:<version>")
}
```

The client library ships as a Kotlin Multiplatform artifact with Android, iOS, and JVM targets — add it to `commonMain` in a KMP project, or directly to an Android app.

## 2. Create and start a client

Create a `LivewireClient`, install the plugins you want, and start it:

```kotlin
val livewireClient = LivewireClient {
  install(DatabasePlugin(context))
  install(NetworkPlugin())
  install(PreferencesPlugin(context))
}

livewireClient.start()
```

`start()` begins broadcasting a discovery packet so the host can find your app, and connects to the host once one is listening. Call `stop()` to shut it down.

!!! tip "Debug builds only"
    Livewire is a development tool. Gate it behind a debug source set or a build-type check so the server never ships in a release build.

## 3. Run the host

Download the host app for your platform (coming soon), or run it from source:

```bash
./gradlew :host:run
```

## 4. Connect

With your app running on a connected device, emulator, or the same machine, the host discovers it automatically:

- **Android** — devices and emulators visible to `adb devices`
- **iOS** — devices connected over USB
- **Desktop** — Livewire apps running on the same machine

Select your app in the host's device list to connect. The host performs an encrypted handshake and your installed plugins appear as panels, live.

## Next steps

- Browse the [existing plugins](plugins/existing.md) you can install today
- Learn how to [build your own plugin](plugins/building.md)
- Curious what's on the wire? See [Connections](connections.md)
