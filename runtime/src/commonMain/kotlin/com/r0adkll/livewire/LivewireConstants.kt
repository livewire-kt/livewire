package com.r0adkll.livewire

object LivewireConstants {
  const val Port = 38301
  const val BridgePort = 38302
  const val UdpDiscoveryPort = 38303
  private const val TcpDiscoveryPortStart = 38304
  private const val TcpDiscoveryPortCount = 5
  const val WsPath: String = "/livewire"

  val TcpDiscoveryPorts: List<Int>
    get() = (TcpDiscoveryPortStart until TcpDiscoveryPortStart + TcpDiscoveryPortCount).toList()
}
