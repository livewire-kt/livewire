# Connections

You don't need anything on this page to *use* Livewire — discovery and connection are automatic. It documents how the pieces fit together, what's on the wire, and the security model.

## Ports

Livewire owns a small block of localhost ports:

| Port | Purpose |
|---|---|
| `38301` | WebSocket connection (`/livewire`) — the host listens here |
| `38302` | iOS physical-device USB bridge |
| `38303` | UDP discovery (desktop apps and iOS simulators) |
| `38304–38308` | TCP discovery (Android and physical iOS devices) |

## Discovery

A running client periodically announces itself with a compact binary **discovery packet**: protocol version, platform, a per-process instance id, app name and package, device name, OS version, and an optional app icon. How the packet travels depends on the platform:

| Client platform | Mechanism |
|---|---|
| Android | Client serves the packet on a device-local TCP port (38304–38308); the host polls connected devices through ADB |
| iOS (physical) | Same TCP approach, polled through `usbmuxd` over USB |
| iOS (simulator) | UDP packet to `127.0.0.1:38303` every 2 seconds (the simulator shares the Mac's loopback) |
| Desktop | Same UDP packet to `127.0.0.1:38303` |

The host merges all three sources into one device list, pruning apps that haven't been seen for a few seconds. Discovery packets carry the **protocol version**; on a mismatch the host disables the Connect button and tells you which side is out of date.

## Connection

Perhaps counterintuitively, **the host runs the WebSocket server** and the client dials in. When you select an app in the host:

1. The host starts (or reuses) its WebSocket server on `127.0.0.1:38301` and sets up the path for the device to reach it:
    - **Android** — an `adb reverse` forward, so the device's `127.0.0.1:38301` tunnels to the host
    - **iOS physical** — a USB bridge through `usbmuxd` (the client app runs a small port forwarder on-device)
    - **iOS simulator / Desktop** — plain loopback, nothing to forward
2. The client's connection loop (which retries every 3 seconds whenever it's disconnected) reaches the host and connects to `ws://127.0.0.1:38301/livewire`, identifying itself with its instance id — the host only accepts the app you selected.
3. Both sides perform the encryption handshake, the client sends its manifest (theme, serialization format, installed plugins), and the host UI goes live.

### Connection state

- The **host** moves through `Disconnected → Forwarding → Listening → Connected` (with `Error` on failure), surfaced as a status chip in the top bar. If the client drops, the host returns to `Listening` and waits.
- The **client** exposes `Stopped → Connecting → Connected` and owns reconnection: it retries every 3 seconds forever until `stop()` is called. Each reconnect performs a fresh handshake.

## On the wire

- **Text frames** carry JSON control messages: the client manifest, plugin selection, dark-mode changes, crash reports, and `LivewireAction` interactions flowing host → client.
- **Binary frames** carry layout data — either a full `LayoutNode` tree or a patch list (inserts, removals, moves, node updates). Trees are encoded with **Protobuf** by default, or JSON if the client opts in via `layoutNodeSerialization(LayoutNodeSerialization.Json)`.
- If the host detects a patch it can't apply (desync), it requests a full tree and the client restarts the stream — you never see a corrupted UI, at worst a refresh.

The host's **network meter window** (toggle in the top bar) visualizes all of this live: frame sizes, cumulative bytes, throughput, and a log of recent protocol messages.

## Security

Every connection is encrypted, with no configuration and no way to turn it off:

- During the handshake, both sides exchange **ephemeral ECDH P-256** public keys and derive **AES-256-GCM** session keys via HKDF-SHA256 — one key per direction, fresh on every connection, never persisted.
- After the handshake, **every frame is encrypted** — control messages and layout data alike. Nonces are counter-based and the receiver rejects replayed or out-of-order frames.
- Discovery packets (app name, device name, icon) are the only unencrypted traffic.

### Trust model

The threat model is *local machine and USB cable*, not the open network:

- Nothing ever binds to a LAN interface. The host's server listens on `127.0.0.1` only; clients dial `127.0.0.1`; discovery is loopback- or cable-scoped (ADB / usbmux). Livewire is invisible on shared Wi-Fi.
- The key exchange is unauthenticated (no certificates or pinning). It protects against passive observation on the local machine, but is not a substitute for network-grade authentication — which is fine for its localhost/USB scope, and a reason not to expose the ports beyond it.
