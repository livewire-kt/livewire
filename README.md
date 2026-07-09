<p align="center">
  <img src=".github/logo.webp" width="200" alt="Livewire logo" />
</p>

<h1 align="center">Livewire</h1>

<p align="center">
  A real-time development bridge that streams live UI from Android, iOS, and Desktop apps to a Desktop host over ADB, USB mux, or localhost using WebSockets.
</p>

---

## How it works

Livewire lets you inspect and interact with a running app's UI remotely. The client app renders a layout tree of `LayoutNode` objects and streams them over an encrypted WebSocket to the desktop host, which renders them as live Compose UI.

1. A **client** app (Android, iOS, or Desktop) starts an embedded WebSocket server
2. The **host** discovers connected devices via ADB, USB mux, or localhost
3. The host forwards the port and connects as a WebSocket client
4. Layout nodes and user actions flow bidirectionally over the connection, with ECDH encryption

## Project Structure

```
livewire/
├── runtime/             # KMP shared code — constants, transport, discovery, crypto, protocol
├── ui/                  # KMP UI library — LayoutNode types, modifiers, serialization
├── client/              # KMP client library (Android/iOS/JVM) — LivewireClient + server
├── host/                # Desktop host app (Compose Desktop) — device management + UI rendering
├── compiler/            # KSP plugin — generates serialization code for nodes & modifiers
├── plugins/
│   ├── database/        # SQLite browser plugin (SQLDelight, platform-specific drivers)
│   ├── playground/      # UI playground plugin
│   └── network/
│       ├── core/        # Network inspection plugin core
│       ├── ktor/        # Ktor HTTP client integration
│       └── okhttp/      # OkHttp client integration
├── demo/
│   ├── common/          # Shared demo code (KMP, SQLDelight)
│   ├── android/         # Android demo app
│   └── desktop/         # Desktop demo app
```

## Architecture

### Discovery

The host discovers running Livewire clients across three transports:

- **ADB** — scans TCP ports 38304–38308 on connected Android devices for `DiscoveryPacket` broadcasts
- **USB mux** — tunnels to iOS devices via `IosForwarder`
- **Localhost** — detects Livewire apps running on the same machine

### Transport & Security

- **Text frames** carry JSON-encoded `UiProtocol` messages and `LivewireAction` payloads
- **Binary frames** carry serialized `LayoutNode` trees (JSON or Protobuf, configurable per connection)
- **Encryption** — ECDH key exchange (P-256) during handshake, HKDF-SHA256 derived session keys, all frames encrypted

### UI System

25+ `LayoutNode` types representing Compose components:

| Category | Nodes |
|---|---|
| Containers | `Box`, `Column`, `Row`, `Surface`, `Card`, `Scaffold` |
| Input | `TextField`, `Checkbox`, `Switch`, `RadioButton`, `Slider` |
| Interactive | `Button`, `IconButton`, `Chip`, `FloatingActionButton` |
| Display | `Text`, `Icon`, `Divider`, `ProgressIndicator`, `Image` |
| Advanced | `Table`, `TabRow`, `DropdownMenu`, `AnimatedVisibility` |

15+ `LivewireModifier` types: `Padding`, `Size`, `Width`, `Height`, `Background`, `Border`, `Alpha`, `Clip`, `Clickable`, `HorizontalScroll`, `VerticalScroll`, `AnimateContentSize`, and more.

### Actions

User interactions flow back from host to client as `LivewireAction` messages:

- `ClickAction` — taps and button presses
- `CheckedChangeAction` — toggles and checkboxes
- `ValueChangeAction`, `IntValueChangeAction`, `FloatValueChangeAction` — text fields, sliders

### Plugin System

Plugins extend Livewire with inspectable tools. Each plugin provides a `@LivewireComposable Content()` panel in the host and is installed via `LivewireClientBuilder.install()`.

| Plugin | Description |
|---|---|
| `database` | Browse SQLite databases on-device |
| `playground` | Interactive UI playground |
| `network:ktor` | Inspect Ktor HTTP traffic |
| `network:okhttp` | Inspect OkHttp HTTP traffic |

## Getting Started

### Prerequisites

- JDK 23+
- Android SDK (compileSdk 36)
- A connected Android device or emulator (via ADB)

### Run the demo

```bash
# Build and install the Android demo app
./gradlew :demo:android:installDebug

# Run the Desktop host
./gradlew :host:run
```

The host will automatically discover the running demo app and connect.

### Build individual modules

```bash
./gradlew :client:assembleDebug
./gradlew :host:jvmMainClasses
./gradlew :ui:jvmMainClasses
```

## License

TBD
