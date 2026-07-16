# Existing Plugins

Livewire ships with a set of first-party plugins. All plugin artifacts are published under the `com.livewire-kt.livewire` group and support Android, JVM (Desktop), and iOS unless noted.

| Plugin | Artifact | What it does |
|---|---|---|
| [Database](#database) | `plugin-database` | Browse SQLite databases on-device and run queries |
| [Network](#network) | `plugin-network-core` (+ `-ktor` / `-okhttp`) | Inspect HTTP traffic from Ktor and OkHttp clients |
| [Preferences](#preferences) | `plugin-preferences` | View and live-edit SharedPreferences, DataStore, NSUserDefaults, and java.util.prefs |
| [Recomposition](#recomposition) | `plugin-recomposition` | Live recomposition counts and invalidation reasons |
| [Playground](#playground) | *(not published)* | Widget catalog used for developing Livewire itself |

## Database

A raw **SQLite** inspector. It works with any SQLite database file, regardless of what created it (SQLDelight, Room, raw SQLite, etc). Browse databases and tables, view schemas, and run SQL queries; `SELECT` / `PRAGMA` / `EXPLAIN` statements are detected and run read-only.

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

## Preferences

A key/value store inspector. View and **live-edit** your app's preferences from the host. Edits are written through the real store APIs in your running app, and changes your app makes appear in the host immediately via each store's native change mechanism (`OnSharedPreferenceChangeListener`, `DataStore.data`, `NSUserDefaultsDidChangeNotification`, `PreferenceChangeListener`).

The constructor is platform-specific, reflecting how stores are located on each platform:

=== "Android"

    ```kotlin
    // Discovers SharedPreferences by scanning the app's shared_prefs/ directory
    install(PreferencesPlugin(context))
    ```

=== "Desktop (JVM)"

    ```kotlin
    // Inspects the given java.util.prefs user-root node paths
    install(PreferencesPlugin("/com/example/myapp"))
    ```

=== "iOS"

    ```kotlin
    // Always includes NSUserDefaults.standardUserDefaults; add app-group suites as needed
    install(PreferencesPlugin(suiteNames = listOf("group.com.example.shared")))
    ```

`DataStore` enforces a single instance per file, so DataStores are never discovered — register the live instances your app already uses via the trailing lambda:

```kotlin
install(
  PreferencesPlugin(context) {
    // Full read/write inspection of a Preferences DataStore
    dataStore("settings", settingsDataStore)

    // Read-only view of any typed DataStore, rendered by the lambda
    protoDataStore("session", sessionStore) { Json.encodeToString(it) }
  }
)
```

In the host: filter entries by key, toggle booleans with a switch, and edit everything else inline — commit with the check button or <kbd>Enter</kbd>, cancel with <kbd>Esc</kbd>. Edits are drafts until committed, so nothing is written while you type, and invalid input (e.g. `abc` for an Int) shows an inline error instead of writing. New entries can be added with any type the selected store supports; deleting a key or clearing a store uses a two-step confirm. String sets are edited one entry per line, byte arrays as Base64.

| Store | Discovery | Types |
|---|---|---|
| SharedPreferences (Android) | Automatic | String, Int, Long, Float, Boolean, StringSet |
| Preferences DataStore | Registered | String, Int, Long, Float, Double, Boolean, StringSet, Bytes |
| NSUserDefaults (iOS) | Standard + registered suites | String, Long, Double, Boolean, StringSet, Bytes |
| java.util.prefs (Desktop) | Registered node paths | String |
| Proto DataStore | Registered | Read-only rendered view |

!!! note "Platform quirks"
    `NSUserDefaults` types are best-effort — `NSNumber` doesn't preserve the exact Kotlin type, so integers surface as Long and floating-point values as Double, and change notifications only fire for in-process writes. `java.util.prefs` values are string-typed on read-back, so everything there is presented as a String.

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
    Recomposition tracking hooks Compose source information, which must be enabled before the first composition. On Android this happens automatically via `androidx.startup`; on other platforms it's handled when the plugin is constructed. Just construct it early (before your first frame).

!!! note "iOS Details"
    Due to the lack of reflection in Kotlin/Native, a bit less detail is available when using the recomposition plugin on the iOS platform.

## Playground

An internal widget catalog exercising every Livewire widget: buttons, chips, text fields, sliders, tabs, tables, animations, and a crash-test button. It isn't published as an artifact, but it's the best reference for what the UI system can do and a good template for your own plugin: [`plugins/playground`](https://github.com/livewire-kt/livewire/tree/main/plugins/playground).
