<p align="center">
  <img src=".github/art/logo.webp" width="200" alt="Livewire logo" />
</p>

<h1 align="center">Livewire</h1>

<p align="center">
  A real-time development bridge that streams live tooling UI from Android, iOS, and Desktop apps to a desktop host over ADB, USB, or localhost.
</p>

---

Livewire embeds a small client in your app that serves debugging tools — database browsing, network inspection, and more — as a stream of UI over an encrypted WebSocket. The desktop host app discovers running clients on connected devices, connects, and renders that UI live. Interactions in the host flow back to the device in real time.

- **Client** — a library you add to your app. It runs an embedded server, broadcasts itself for discovery, and streams plugin UI to the host.
- **Host** — a desktop app you run on your machine. It finds clients over ADB (Android), USB (iOS), or localhost (Desktop), and renders whatever they serve.

## Quick Start

> 🚧 Livewire is a work in progress — published artifacts and host app downloads are not available yet.

### 1. Add the client to your app

```kotlin
// build.gradle.kts
dependencies {
  implementation("com.livewire-kt:client:<version>") // Coming soon
}
```

Create a client, install the plugins you want, and start it:

```kotlin
val livewireClient = LivewireClient {
  install(DatabasePlugin(context))
  install(NetworkPlugin())
}

livewireClient.start()
```

### 2. Run the host

Download the host app for your platform (coming soon), or run it from source:

```bash
./gradlew :host:run
```

With your app running on a connected device, emulator, or the same machine, the host discovers it automatically — select it to connect.

## License

```
Copyright 2026 livewire-kt

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
