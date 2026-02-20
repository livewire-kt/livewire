# Livewire

Live development bridge between a Desktop (Host) app and an Android (Client) app over ADB using WebSockets.

## Project Structure

```
livewire/
├── shared/              # KMP shared code (constants, platform expect/actual)
├── client-runtime/      # Android WebSocket server (Ktor CIO embedded server)
├── client/              # Android client library (KMP, android target)
├── composeApp/          # Android app (Compose Multiplatform, hosts LivewireServer)
├── host-runtime/        # Desktop WebSocket client + ADB forwarding (JVM only)
├── host/                # Desktop app (Compose Desktop, hosts LivewireHostConnection)
├── localserver/         # Ktor dev server (Netty, for local testing)
└── gradle/              # Version catalog (libs.versions.toml)
```

### Module Dependency Graph
- `:composeApp` → `:shared`, `:client-runtime` (androidMain only)
- `:host` → `:shared`, `:host-runtime` (jvmMain only)
- `:client-runtime` → `:shared`
- `:host-runtime` → `:shared`

## Tech Stack

- **Kotlin**: 2.3.0 (Kotlin Multiplatform)
- **Compose Multiplatform**: 1.10.0
- **Ktor**: 3.3.3 (CIO server on Android, CIO client on Desktop)
- **Gradle**: Configuration cache + build cache enabled
- **Android**: compileSdk 36, minSdk 24, JVM target 11
- **Desktop JVM**: Toolchain 23

## Key Architecture

### WebSocket Communication (over ADB)
1. Android app runs an embedded Ktor CIO WebSocket server on port `38301` at path `/livewire`
2. Desktop app uses `adb forward tcp:38301 tcp:38301` to bridge the port
3. Desktop connects as a Ktor WebSocket client to `ws://localhost:38301/livewire`
4. Constants defined in `shared/.../Constants.kt`: `LIVEWIRE_PORT`, `LIVEWIRE_WS_PATH`

### Key Classes
- `LivewireServer` (client-runtime, androidMain) — embedded CIO server, exposes `connectionState: StateFlow` and `incomingMessages: SharedFlow`
- `LivewireHostConnection` (host-runtime) — manages ADB forward + WebSocket client, exposes `connectionState: StateFlow` and `incomingMessages: SharedFlow`
- `AdbForwarder` (host-runtime) — runs `adb forward` / `adb forward --remove` via ProcessBuilder

## Build & Run Commands

```bash
# Build Android app
./gradlew :composeApp:assembleDebug

# Run Desktop host app
./gradlew :host:run

# Build specific modules
./gradlew :host:jvmMainClasses
./gradlew :client-runtime:assembleDebug
```

## Conventions

- Version catalog in `gradle/libs.versions.toml` — all dependency versions centralized there
- Typesafe project accessors enabled (`projects.shared`, `projects.hostRuntime`, etc.)
- Package root: `com.r0adkll.livewire` (runtime modules use `.runtime` subpackage)
- Android app package: `com.r0adkll.livewire.livewire`
- KMP modules use `androidMain` / `jvmMain` / `commonMain` source set layout
- JVM-only modules (`:host-runtime`) use standard `src/main/kotlin` layout
