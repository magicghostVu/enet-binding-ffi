package magicghostvu.enetffi

import java.lang.foreign.MemoryLayout
import java.lang.foreign.StructLayout
import java.lang.foreign.ValueLayout
import java.lang.invoke.VarHandle

object EnetStructLayout {
    val structEnetEvent = StructEnetEvent
    val structEnetPacket = StructEnetPacket
    val structEnetAddress = StructEnetAddress
}

object StructEnetEvent {
    val layout: StructLayout = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("type"),
        MemoryLayout.paddingLayout(4),
        ValueLayout.ADDRESS.withName("peer"),
        ValueLayout.JAVA_BYTE.withName("channelID"),
        MemoryLayout.paddingLayout(3),
        ValueLayout.JAVA_INT.withName("data"),
        ValueLayout.ADDRESS.withName("packet")
    )

    val byteSize: Long = layout.byteSize()

    val handleGetData: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("data"),
    )

    val handleGetPeerAddress: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("peer"),
    )

    val varHandleReadType: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("type"),
    )

    val handleGetChannelId: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("channelID"),
    )

    val handleGetPacketAddress: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("packet"),
    )
}

object StructEnetPacket {
    val layout: StructLayout = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG.withName("referenceCount"),
        ValueLayout.JAVA_INT.withName("flags"),
        MemoryLayout.paddingLayout(4),
        ValueLayout.ADDRESS.withName("data"),
        ValueLayout.JAVA_LONG.withName("dataLength"),
        ValueLayout.ADDRESS.withName("freeCallback"),
        ValueLayout.ADDRESS.withName("userData"),
    )

    val byteSize: Long = layout.byteSize()

    val packetFlagsHandle: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("flags"),
    )

    val dataLengthHandle: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("dataLength"),
    )

    val nativeDataBufferHandle: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("data"),
    )
}

object StructEnetAddress {
    val layout: StructLayout = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("host"),
        ValueLayout.JAVA_SHORT.withName("port"),
        MemoryLayout.paddingLayout(2),
    )

    val handlePort: VarHandle = layout.varHandle(
        MemoryLayout.PathElement.groupElement("port")
    )
}