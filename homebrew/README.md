# Homebrew distribution — Livewire

The desktop app (built from the `:host` module) is distributed as a macOS
**Homebrew Cask** that downloads the pre-built `.dmg` from this repo's GitHub
Releases.

```
livewire (this repo, public)
  └── .github/workflows/release-host.yml   builds .dmg on release, attaches to the Release
homebrew-tap (separate public repo: livewire-kt/homebrew-tap)
  └── Casks/livewire.rb                     points brew at the released .dmg
```

Once set up, users install with:

```bash
brew install --cask livewire-kt/tap/livewire
# or:
brew tap livewire-kt/tap
brew install --cask livewire
```

`Casks/livewire.rb` and `update-cask.sh` in this directory are the source of
truth — copy them into the tap repo when you create it.

## How a release works

1. Cut a GitHub Release on `livewire-kt/livewire` with a **numeric** tag, e.g. `v1.0.0`
   (jpackage rejects non-numeric versions like `v1.0.0-beta`, and macOS rejects a
   `0.x` major).
2. `release-host.yml` runs on `macos-latest`, builds `:host:packageDmg`, renames the
   output to `Livewire-<version>.dmg`, and attaches it to the Release.
3. The workflow prints the `version` + `sha256` to the job summary, and — if a
   `HOMEBREW_TAP_TOKEN` secret is configured — bumps `Casks/livewire.rb` in the
   tap repo automatically. Without that token, bump the cask manually (below).

## Manual cask update

From a checkout of the tap repo:

```bash
./update-cask.sh 1.0.0        # downloads the released dmg, fills in version + sha256
git add Casks/livewire.rb && git commit -m "livewire 1.0.0" && git push
```

## Not yet notarized

The `.dmg` is currently **unsigned and un-notarized**. Homebrew still installs it, but
macOS Gatekeeper quarantines it on first launch. The cask's `caveats` tell users to run:

```bash
xattr -dr com.apple.quarantine "/Applications/Livewire.app"
```

To remove this friction, get an Apple Developer ID, fill in the `signing` /
`notarization` blocks in `host/build.gradle.kts`, and add these repo secrets so the
release workflow can sign: `MACOS_SIGNING_IDENTITY`, `MACOS_NOTARIZATION_APPLE_ID`,
`MACOS_NOTARIZATION_PASSWORD`, `MACOS_NOTARIZATION_TEAM_ID`. Then drop the `caveats`
block from the cask.
