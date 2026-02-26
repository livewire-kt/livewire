import UIKit
import ComposeApp

final class AppDelegate: NSObject, UIApplicationDelegate {
  private let portForwarder = LivewirePortForwardingServer()

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    #if !targetEnvironment(simulator)
    portForwarder.start(withPort: UInt(LivewireConstants.shared.Port), multiplexPort: UInt(LivewireConstants.shared.MultiplexPort))
    #endif
    return true
  }

  func applicationWillTerminate(_ application: UIApplication) {
    #if !targetEnvironment(simulator)
    portForwarder.stop()
    #endif
  }
}
