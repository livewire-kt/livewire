import SwiftUI

@main
struct LivewireIOSApp: App {
  @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

  var body: some Scene {
    WindowGroup {
      ComposeView()
        .ignoresSafeArea()
    }
  }
}
