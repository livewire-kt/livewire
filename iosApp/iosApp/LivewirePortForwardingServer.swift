import Foundation
import CocoaAsyncSocket
import Peertalk
import UIKit

final class LivewirePortForwardingServer: NSObject {
  private enum FrameType: UInt32 {
    case openPipe = 201
    case writeToPipe = 202
    case closePipe = 203
  }

  private let socketQueue = DispatchQueue(label: "LivewirePortForwardingServer")
  private var serverChannel: PTChannel?
  private var peerChannel: PTChannel? {
    didSet {
      if peerChannel == nil {
        clearClientSockets()
      }
    }
  }
  private var protocolHandler: PTProtocol
  private var clientSockets: [UInt32: GCDAsyncSocket] = [:]
  private var forwardPort: UInt?
  private var multiplexPort: UInt?

  override init() {
    protocolHandler = PTProtocol(dispatchQueue: socketQueue)
    super.init()
  }

  func start(withPort port: UInt, multiplexPort: UInt) {
    self.forwardPort = port
    self.multiplexPort = multiplexPort
    listenForMultiplexingChannel(onPort: multiplexPort)
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(restartListeners),
      name: UIApplication.didBecomeActiveNotification,
      object: nil
    )
  }

  func stop() {
    NotificationCenter.default.removeObserver(self)

    peerChannel?.close()
    peerChannel = nil
    serverChannel?.close()
    serverChannel = nil
    clearClientSockets()
  }

  @objc private func restartListeners() {
    if let port = multiplexPort {
      listenForMultiplexingChannel(onPort: port)
    }
  }

  private func listenForMultiplexingChannel(onPort port: UInt) {
    if let existing = serverChannel, existing.isListening { return }

    serverChannel?.close()
    serverChannel = nil

    let channel = PTChannel(protocol: protocolHandler, delegate: self)
    channel.listen(on: in_port_t(port), IPv4Address: INADDR_LOOPBACK) { error in
      if let error = error {
        print("Failed to listen on 127.0.0.1:%lu: %@", port, error as NSError)
      } else {
        self.serverChannel = channel
          print("Listening on 127.0.0.1:%lu", port)
      }
    }
  }

  private func sendFrame(type: FrameType, tag: UInt32, payload: Data?) {
    guard let channel = peerChannel else { return }

    channel.sendFrame(type: type.rawValue, tag: tag, payload: payload ?? Data())
  }
}

extension LivewirePortForwardingServer: PTChannelDelegate {
  func channel(_ channel: PTChannel, didAcceptConnection otherChannel: PTChannel, from address: PTAddress) {
    peerChannel?.cancel()
    peerChannel = otherChannel
    peerChannel?.userInfo = address
    print("Connected to %@", address)
  }

  func channel(_ channel: PTChannel, didRecieveFrame type: UInt32, tag: UInt32, payload: Data?) {
    guard let frameType = FrameType(rawValue: type) else { return }

    switch frameType {
    case .openPipe:
      guard let port = forwardPort else { return }
      let socket = GCDAsyncSocket(delegate: self, delegateQueue: socketQueue)
      socket.userData = NSNumber(value: tag)
      clientSockets[tag] = socket
      do {
        try socket.connect(toHost: "127.0.0.1", onPort: UInt16(port))
      } catch {
        print("Failed to connect to 127.0.0.1:%lu for pipe %u: %@", port, tag, error as NSError)
        clientSockets.removeValue(forKey: tag)
        sendFrame(type: .closePipe, tag: tag, payload: nil)
      }
    case .writeToPipe:
      guard let socket = clientSockets[tag], let payload = payload else { return }
      socket.write(payload, withTimeout: -1, tag: 0)
    case .closePipe:
      clientSockets[tag]?.disconnectAfterWriting()
    }
  }

  func channelDidEnd(_ channel: PTChannel, error: Error?) {
    if let error = error {
      print("Channel ended with error: %@", error as NSError)
    } else {
      print("Channel ended")
    }
    peerChannel = nil
    clearClientSockets()
  }
}

extension LivewirePortForwardingServer: GCDAsyncSocketDelegate {
  func socket(_ sock: GCDAsyncSocket, didConnectToHost host: String, port: UInt16) {
    let socketTag = (sock.userData as? NSNumber)?.uint32Value ?? 0
    print("Connected to 127.0.0.1:%hu for pipe %u", port, socketTag)
    sock.readData(withTimeout: -1, tag: 0)
  }

  func socket(_ sock: GCDAsyncSocket, didRead data: Data, withTag tag: Int) {
    let socketTag = (sock.userData as? NSNumber)?.uint32Value ?? 0
    sendFrame(type: .writeToPipe, tag: socketTag, payload: data)
    sock.readData(withTimeout: -1, tag: 0)
  }

  func socketDidDisconnect(_ sock: GCDAsyncSocket, withError err: Error?) {
    let socketTag = (sock.userData as? NSNumber)?.uint32Value ?? 0
    if let err = err {
      print("Socket %u disconnected with error: %@", socketTag, err as NSError)
    } else {
      print("Socket %u disconnected", socketTag)
    }
    clientSockets.removeValue(forKey: socketTag)
    sendFrame(type: .closePipe, tag: socketTag, payload: nil)
  }
}

private extension LivewirePortForwardingServer {
  func disconnect(_ socket: GCDAsyncSocket) {
    socket.setDelegate(nil, delegateQueue: nil)
    socket.disconnect()
  }

  func clearClientSockets() {
    clientSockets.values.forEach { disconnect($0) }
    clientSockets.removeAll()
  }
}
