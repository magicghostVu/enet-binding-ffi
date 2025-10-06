package magicghostvu.enetffi

enum class EnetEventType(val code: Int) {
    ENET_EVENT_TYPE_NONE(0),
    ENET_EVENT_TYPE_CONNECT(1),
    ENET_EVENT_TYPE_DISCONNECT(2),
    ENET_EVENT_TYPE_RECEIVE(3);


    companion object {
        private val mapCached = entries
            .associateBy {
                it.code
            }

        fun fromCode(code: Int): EnetEventType {
            return mapCached.getValue(code)
        }
    }
}