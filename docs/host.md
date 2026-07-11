# Host App

The host is a Compose Desktop app that discovers running Livewire clients, connects to them, and renders their plugin UI.

## Running

Download the host app for your platform (coming soon), or run it from source:

```bash
./gradlew :host:run
```

Native installers can be built with Compose Desktop packaging:

```bash
./gradlew :host:packageDistributionForCurrentOS   # .dmg / .msi / .deb
```

## Connecting to an app

On launch the host shows every discovered client — Android devices and emulators (via ADB), physical iOS devices (via USB), and desktop apps on the same machine — each with its app icon, name, and device. The list updates live as apps start and stop.

Click an app to connect. A status chip in the top bar tracks the connection: **Forwarding** (setting up the ADB/USB bridge) → **Listening** (waiting for the client) → **Connected**. If the app disappears — a crash, a redeploy — the host keeps listening and reconnects automatically when it comes back.

!!! tip "Protocol version"
    If an app shows a warning instead of a Connect button, its Livewire library and your host speak different protocol versions — update whichever side is older.

## The main window

- **Plugin drawer** — a collapsible left drawer lists the connected app's installed plugins, using the icons and titles the app supplied. Select one to stream its UI; it renders full-window, live.
- **Dark mode toggle** — restyles the host *and* notifies the client, so plugin UI follows along. Rendered plugin UI uses the connected app's own theme colors, sent during the handshake.
- **Network meter** — opens a separate window with live charts of layout frame sizes, cumulative bytes, and throughput, plus a log of recent protocol messages. Useful when developing plugins to see what your UI costs on the wire.
- **Crash recovery** — if a plugin throws on-device, the host shows a snackbar with a **Reload** action; the app itself is unaffected.

Window size, position, dark mode, drawer state, and the last-connected app are remembered between launches.
