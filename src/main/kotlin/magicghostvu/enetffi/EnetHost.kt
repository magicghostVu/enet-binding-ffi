package magicghostvu.enetffi

import java.lang.foreign.MemorySegment

sealed class EnetHost {
    internal abstract val hostAddress: MemorySegment
    internal abstract val reuseEventPointer: MemorySegment

    protected var destroyed = false

    fun destroy() {
        if (!destroyed) {
            Enet.destroyHost(this)
            Enet.freeEnetEvent(reuseEventPointer)
            destroyed = true
        } else throw IllegalStateException("Already destroyed")
    }

    fun update(maxDelayMs: UInt): EnetEvent {
        if (destroyed) {
            // or throw exception???
            return NoneEvent
        }
        return Enet.hostService(this, maxDelayMs)
    }
}