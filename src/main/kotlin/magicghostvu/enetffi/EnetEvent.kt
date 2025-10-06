package magicghostvu.enetffi

import java.nio.ByteBuffer

sealed class EnetEvent {
}

data object NoneEvent : EnetEvent()

class ConnectEvent(
    val peerConnect: EnetPeer,
    val dataConnect: Int
) : EnetEvent() {}


class DisconnectEvent(
    val peerDisconnect: EnetPeer,
    val dataDisconnect: Int
) : EnetEvent() {}

class ReceivePacketEvent(
    val peer: EnetPeer,
    val channelId: UByte,
    val data: ByteBuffer,
    val flags: Int,
) : EnetEvent() {}