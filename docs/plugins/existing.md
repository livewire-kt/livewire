# Existing Plugins

Livewire ships with a set of first-party plugins. All plugin artifacts are published under the `com.livewire-kt.livewire` group and support Android, JVM (Desktop), and iOS unless noted.

| Plugin | Artifact | What it does |
|---|---|---|
| [Database](#database) | `plugin-database` | Browse SQLite databases on-device and run queries |
| [Network](#network) | `plugin-network-core` (+ `-ktor` / `-okhttp`) | Inspect HTTP traffic from Ktor and OkHttp clients |
| [Recomposition](#recomposition) | `plugin-recomposition` | Live recomposition counts and invalidation reasons |
| [Playground](#playground) | *(not published)* | Widget catalog used for developing Livewire itself |

## Database

A raw **SQLite** inspector — it works with any SQLite database file, regardless of what created it (SQLDelight, Room, raw SQLite). Browse databases and tables, view schemas, and run SQL queries; `SELECT` / `PRAGMA` / `EXPLAIN` statements are detected and run read-only.

The constructor is platform-specific, reflecting how databases are located on each platform:

=== "Android"

    ```kotlin
    // Discovers databases via context.databaseList()
    install(DatabasePlugin(context))
    ```

=== "Desktop (JVM)"

    ```kotlin
    // Scans the given directories for database files
    install(DatabasePlugin(".", "data/databases"))
    ```

=== "iOS"

    ```kotlin
    install(DatabasePlugin())
    ```

## Network

The network inspector comes in two parts: the **plugin** that renders the request list in the host, and an **integration** that hooks into your HTTP client and records traffic.

Install the plugin:

```kotlin
install(NetworkPlugin())
```

Then add an integration for each HTTP client your app uses:

=== "Ktor"

    Add `livewire-plugins-network-ktor` and install the Ktor client plugin:

    ```kotlin
    val httpClient = HttpClient {
      install(LivewireNetworkPlugin) {
        maxBodySize = 256L * 1024 // default
      }
    }
    ```

=== "OkHttp"

    Add `livewire-plugins-network-okhttp` (Android/JVM only) and register the interceptor:

    ```kotlin
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(LivewireNetworkInterceptor())
      .build()
    ```

Both integrations feed the same collector, so requests from multiple clients appear in one unified timeline. Request and response bodies are captured up to `maxBodySize` (256 KiB by default).

## Recomposition

Live Compose performance tooling: per-composable recomposition, skip, and child-recomposition counts rendered as an expandable tree, with a detail panel showing invalidation reasons and parameter changes.

```kotlin
install(RecompositionPlugin())
```

The constructor exposes tuning knobs:

```kotlin
RecompositionPlugin(
  alwaysOnSampling = false, // if false, tracking only runs while the plugin is open
  recompositionThresholds = RecompositionThresholds(low = 2, moderate = 5, high = 10),
)
```

!!! note "Initialization"
    Recomposition tracking hooks Compose source information, which must be enabled before the first composition. On Android this happens automatically via `androidx.startup`; on other platforms it's handled when the plugin is constructed — just construct it early (before your first frame).

## Playground

An internal widget catalog exercising every Livewire widget — buttons, chips, text fields, sliders, tabs, tables, animations, and a crash-test button. It isn't published as an artifact, but it's the best reference for what the UI system can do and a good template for your own plugin: [`plugins/playground`](https://github.com/livewire-kt/livewire/tree/main/plugins/playground).
