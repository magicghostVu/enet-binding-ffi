package magicghostvu.enetffi

import java.lang.foreign.MemorySegment

class EnetClientHost internal constructor(
    internal override val hostAddress: MemorySegment,
    val serverPeer: EnetPeer
) : EnetHost() {
    override val reuseEventPointer: MemorySegment = Enet.createEnetEvent();

}