cask "livewire" do
  version "1.0.0"
  sha256 "0000000000000000000000000000000000000000000000000000000000000000"

  url "https://github.com/livewire-kt/livewire/releases/download/v#{version}/Livewire-#{version}.dmg"
  name "Livewire"
  desc "Live development bridge that streams app UI to a desktop host"
  homepage "https://github.com/livewire-kt/livewire"

  # Raise the floor once notarization is in place; big_sur is Compose Desktop's
  # practical minimum.
  depends_on macos: :big_sur

  app "Livewire.app"

  zap trash: [
    "~/Library/Application Support/Livewire",
    "~/Library/Caches/com.livewire.host",
    "~/Library/Preferences/com.livewire.host.plist",
  ]

  # The app is not yet signed/notarized, so first launch is blocked by Gatekeeper.
  # Until an Apple Developer ID is wired up, users clear the quarantine flag once:
  caveats <<~EOS
    Livewire is not yet notarized by Apple. If macOS reports the app is
    "damaged" or refuses to open it, clear the quarantine flag once:

      xattr -dr com.apple.quarantine "/Applications/Livewire.app"
  EOS
end
