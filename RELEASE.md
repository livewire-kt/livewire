# Release Process

## Versioning model

- `main` always carries the **next version to ship** with a `-SNAPSHOT` suffix in
  `gradle.properties` (e.g. `livewire.version=1.0.4-SNAPSHOT`).
- Release versions are derived **from the release tag**, never from `gradle.properties`.
- Release tags are `v`-prefixed numeric versions: `v1.0.4`. The `publish` workflow
  validates this and fails on anything else (jpackage requires a plain
  `MAJOR[.MINOR][.PATCH]` version, which is the tag with the `v` stripped).

## Snapshots (automatic)

Every push to `main` runs the [`snapshot`](.github/workflows/snapshot.yml) workflow,
which publishes all library modules to the Central Portal snapshot repository at the
version in `gradle.properties`.

- The workflow fails if `livewire.version` does not end in `-SNAPSHOT` — that's the
  guard against `main` drifting off the snapshot model.
- Consumers pull snapshots by adding the repository:

  ```kotlin
  repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
  }
  ```

## Cutting a release

1. Make sure `main` is green and `gradle.properties` holds the version you want to
   ship as `X.Y.Z-SNAPSHOT`.
2. Create a GitHub release with tag `vX.Y.Z` (e.g. `v1.0.4`) targeting `main`, with
   release notes.
3. Publishing the release triggers the [`publish`](.github/workflows/publish.yml)
   workflow, which:
   1. Validates the tag format and resolves the version (fails fast on a bad tag).
   2. Runs the full test suite.
   3. Publishes all library modules to Maven Central at the tag version and releases
      the deployment (`publishAndReleaseToMavenCentral -Plivewire.version=X.Y.Z`).
   4. Builds the host `.dmg` (`-Plivewire.hostVersion=X.Y.Z`), codesigns, notarizes,
      and staples it (when signing secrets are configured), and uploads
      `Livewire-X.Y.Z.dmg` to the GitHub release.
   5. Bumps the version and sha256 in the
      [homebrew-tap](https://github.com/livewire-kt/homebrew-tap) cask.
   6. Commits `livewire.version=X.Y.(Z+1)-SNAPSHOT` back to `main` (the `bump-version`
      job), so `main` is immediately on the next snapshot.
4. There is no manual post-release step. If you plan a minor/major next instead of the
   auto-bumped patch, push a commit updating `gradle.properties` (e.g. to
   `1.1.0-SNAPSHOT`) whenever you like — the auto-bump never downgrades a version
   that is already ahead.

## Notes & troubleshooting

- **Local dmg builds** work while `main` is on a snapshot: the host build strips the
  `-SNAPSHOT` suffix for `packageVersion` (jpackage rejects non-numeric versions).
- **Auto-bump doesn't trigger a snapshot publish**: the bump commit is pushed with
  `GITHUB_TOKEN`, which GitHub excludes from triggering workflows. The first
  `X.Y.(Z+1)-SNAPSHOT` artifact appears with the next real merge to `main`.
- **Snapshot publishing 403s**: SNAPSHOTs must be enabled for the `com.livewire-kt`
  namespace at [central.sonatype.com](https://central.sonatype.com) (namespace
  settings → Enable SNAPSHOTs).
- **Unsigned dmg**: if `MACOS_CERTIFICATE_P12` isn't set, the workflow builds an
  unsigned dmg and skips notarization. Cask bumping is likewise skipped without
  `HOMEBREW_TAP_DEPLOY_KEY` — update the tap manually via its `update-cask.sh`.
- **Auto-bump fails to push**: if `main` gains branch protection blocking direct
  pushes, the `bump-version` job will fail; bump `gradle.properties` manually (or
  exempt the token).

### Required repository secrets

| Secret | Used for |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` | Central Portal publishing (releases + snapshots) |
| `SIGNING_IN_MEMORY_KEY` / `SIGNING_IN_MEMORY_KEY_ID` / `SIGNING_IN_MEMORY_KEY_PASSWORD` | GPG signing of maven artifacts |
| `MACOS_CERTIFICATE_P12` / `MACOS_CERTIFICATE_PASSWORD` / `MACOS_SIGNING_IDENTITY` | Codesigning the host dmg (optional) |
| `MACOS_NOTARY_KEY_P8` / `MACOS_NOTARY_KEY_ID` / `MACOS_NOTARY_ISSUER_ID` | Notarization (optional) |
| `HOMEBREW_TAP_DEPLOY_KEY` | Pushing the cask bump to homebrew-tap (optional) |
