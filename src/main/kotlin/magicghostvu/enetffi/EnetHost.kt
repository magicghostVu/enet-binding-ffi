package magicghostvu.enetffi

import java.lang.foreign.MemorySegment

// maybe value type
class EnetHost internal constructor(internal val hostAddress: MemorySegment) {

    private var destroyed = false

    internal val reuseEventPointer: MemorySegment = Enet.createEnetEvent();

    fun destroy() {
        if (!destroyed) {
            Enet.destroyHost(this)
            Enet.freeEnetEvent(reuseEventPointer)
            destroyed = true
        } else throw IllegalStateException("Already destroyed")
    }

    fun update(maxDelayMs: UInt): EnetEvent {
        if (destroyed) {
            return NoneEvent
        }
        return Enet.hostService(this, maxDelayMs)
    }

}
