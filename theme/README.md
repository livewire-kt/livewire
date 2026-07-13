# Livewire Theme

A full IDE theme plugin for JetBrains IDEs (IntelliJ IDEA, Android Studio, etc.)
built around the Livewire brand colors — primary red `#F40B0B` and secondary
orange `#FFA500`.

Includes two themes, each pairing a UI theme with a matching editor color scheme:

- **Livewire Dark** — near-black ember-warm UI, red accents, orange links
- **Livewire Light** — warm paper UI, red accents, burnt-amber links

## Building

This is a standalone Gradle build (intentionally not part of the root Livewire
build). From this directory:

```bash
../gradlew buildPlugin
```

The distributable zip lands in `build/distributions/livewire-theme-<version>.zip`.

## Installing

1. Build the plugin (above), or grab the zip from a release.
2. In the IDE: **Settings → Plugins → ⚙️ → Install Plugin from Disk…** and pick the zip.
3. Restart, then pick **Livewire Dark** or **Livewire Light** under
   **Settings → Appearance & Behavior → Appearance → Theme**.

Selecting a theme automatically applies its matching editor color scheme.

## Development

- UI themes: `src/main/resources/themes/*.theme.json`
- Editor schemes: `src/main/resources/themes/*.icls`
- Test in a sandbox IDE with `../gradlew runIde`

The standalone `.icls` files in `docs/assets/themes/` are kept in sync by hand —
if you tune colors here, copy them back there (and vice versa).
