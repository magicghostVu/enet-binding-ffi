package magicghostvu.enetffi

import java.lang.foreign.MemorySegment

// maybe value type
class EnetServerHost internal constructor(override val hostAddress: MemorySegment): EnetHost() {

    override val reuseEventPointer: MemorySegment = Enet.createEnetEvent();


}
