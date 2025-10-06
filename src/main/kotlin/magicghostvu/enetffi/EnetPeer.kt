package magicghostvu.enetffi

import java.lang.foreign.MemorySegment


// make this class can be a key of a hash map
// value type
class EnetPeer internal constructor(
    internal val nativeAddress: MemorySegment
) {
    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else {
            if (other is EnetPeer) {
                nativeAddress.address() == other.nativeAddress.address()
            } else false
        }
    }

    override fun hashCode(): Int {
        return nativeAddress.address().toInt()
    }

    fun sendData(data: ByteArray, offset: Int, dataLen: Int, flags: Int, channelId: UByte): Int {
        // thêm 2 flags, coi như mặc định
        var flagsUpdate = flags or EnetFlag.ENET_PACKET_FLAG_NO_ALLOCATE
        flagsUpdate = flagsUpdate or EnetFlag.ENET_PACKET_FLAG_UNRELIABLE_FRAGMENT
        return Enet.peerSend(
            this,
            data,
            offset.toUInt(),
            dataLen.toUInt(),
            flagsUpdate,
            channelId,
        )
    }

    // todo: maybe add some function get host, port...
}