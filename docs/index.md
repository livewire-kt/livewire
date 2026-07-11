# About

<p align="center">
  <img src="assets/logo.webp" width="220" alt="Livewire logo" />
</p>

**Livewire** is a real-time development bridge for Kotlin Multiplatform apps. It embeds a small client in your Android, iOS, or Desktop app that serves debugging tools — database browsing, network inspection, and more — as a stream of UI over an encrypted WebSocket. A desktop **host** app discovers running clients on connected devices, connects, and renders that tooling UI live. Interactions in the host flow back to the device in real time.

## Why Livewire?

Most on-device debugging tools force a trade-off: either you bundle a heavyweight inspector UI into your app, or you tether yourself to IDE-specific tooling. Livewire takes a different approach:

- **The app owns the tools, the host owns the screen.** Plugins run inside your app with full access to its runtime — its databases, its HTTP clients, its state. Only the *UI* is streamed to the desktop, as a lightweight tree of layout nodes rendered by the host with Compose.
- **No screen real estate stolen.** Your app's UI stays untouched. All tooling lives in the host window on your desk.
- **Multiplatform by design.** One plugin, written once in Kotlin, works across Android, iOS, and Desktop targets.
- **Encrypted by default.** Every connection performs an ECDH key exchange during its handshake; all frames are encrypted with derived session keys.

## How it works

```
┌─────────────────────┐                      ┌─────────────────────┐
│   Your app (client) │                      │  Livewire host app  │
│                     │   discovery packet   │                     │
│  LivewireClient     │ ───────────────────► │  Device list        │
│   ├─ DatabasePlugin │                      │                     │
│   ├─ NetworkPlugin  │   encrypted ws       │  Rendered plugin UI │
│   └─ your plugin    │ ◄──────────────────► │  (live Compose)     │
└─────────────────────┘  ADB / USB / local   └─────────────────────┘
```

1. Your app creates a `LivewireClient`, installs plugins, and starts it. The client broadcasts a discovery packet announcing itself.
2. The host scans for clients over **ADB** (Android devices), **USB** (iOS devices), and **localhost** (desktop apps on the same machine).
3. When you connect, the two sides perform an encrypted handshake, then plugin UI streams to the host and your interactions stream back.

## Project status

!!! warning "Work in progress"
    Livewire is under active development. Published artifacts and host app downloads are not available yet — for now, everything runs from source. APIs may change without notice.

## License

Livewire is released under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
