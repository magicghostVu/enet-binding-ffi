package magicghostvu.enetffi

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer


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

    //request disconnect, gói tin (tin cậy) đang gửi dở sẽ được gửi xong
    // rồi mới disconnect
    // các gói tin trong queue chưa gửi sẽ bị discard
    // sẽ nhận được sự kiện disconnect cho peer này
    // sau khi gọi sẽ không thể gửi gói tin qua peer này được nữa (sẽ không được enqueue)
    fun requestDisconnect(data: Int) {
        Enet.disconnectPeer(this, data)
    }

    // sẽ disconnect khi tất cả queue được gửi hết
    // sau khi gọi sẽ không thể gửi gói tin qua peer này được nữa
    // (sẽ không được enqueue)
    fun requestPeerDisconnectLater(data: Int) {
        Enet.disconnectPeerLater(this, data)
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


    // assume data buffer is already in read mode
    fun sendData(data: ByteBuffer, flags: Int, channelId: UByte): Int {
        // not send if empty
        if (data.limit() == 0) {
            throw IllegalArgumentException("can not send empty data")
        }

        val bufferToWrapArray: ByteBuffer = if (data.hasArray()) {
            data
        } else {

            // allocate new heap buffer
            val newBuffer = ByteBuffer.allocate(data.limit())
            // do copy
            newBuffer.put(data)

            //convert to read mode
            newBuffer.flip()
            newBuffer
        }
        val dataLen = bufferToWrapArray.limit() - bufferToWrapArray.position()
        return sendData(bufferToWrapArray.array(), bufferToWrapArray.position(), dataLen, flags, channelId)
    }
    // todo: maybe add some function get host, port...
}