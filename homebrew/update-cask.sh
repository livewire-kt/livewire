#!/usr/bin/env bash
#
# Updates Casks/livewire.rb to a released version by downloading the
# published .dmg and computing its sha256. Run this from a checkout of the tap
# repo (livewire-kt/homebrew-tap), or point --cask at the file to edit.
#
# Usage:
#   ./update-cask.sh 1.0.0
#   ./update-cask.sh 1.0.0 --cask Casks/livewire.rb
#
set -euo pipefail

VERSION="${1:?usage: update-cask.sh <version> [--cask <path>]}"
VERSION="${VERSION#v}"

CASK="Casks/livewire.rb"
if [ "${2:-}" = "--cask" ]; then
  CASK="${3:?--cask requires a path}"
fi
[ -f "$CASK" ] || { echo "error: cask file not found: $CASK" >&2; exit 1; }

URL="https://github.com/livewire-kt/livewire/releases/download/v${VERSION}/Livewire-${VERSION}.dmg"

echo "Fetching $URL"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
curl -fSL "$URL" -o "$TMP"
SHA="$(shasum -a 256 "$TMP" | awk '{print $1}')"
echo "sha256 = $SHA"

# BSD (macOS) and GNU sed differ on -i; handle both.
if sed --version >/dev/null 2>&1; then
  SED_INPLACE=(-i)
else
  SED_INPLACE=(-i '')
fi

sed "${SED_INPLACE[@]}" -E "s|^  version \".*\"|  version \"$VERSION\"|" "$CASK"
sed "${SED_INPLACE[@]}" -E "s|^  sha256 \".*\"|  sha256 \"$SHA\"|" "$CASK"

echo "Updated $CASK -> version $VERSION"
echo "Review, commit, and push:"
echo "  git add $CASK && git commit -m 'livewire $VERSION' && git push"
