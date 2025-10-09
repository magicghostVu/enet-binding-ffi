package magicghostvu.enetffi

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

fun main() {
    System.setProperty("log4j.configurationFile", "./log4j2.xml")
    val logger = LoggerFactory.getLogger("main")
    val resInit = Enet.init()
    logger.info("res init enet is {}", resInit)

    val serverHost = Enet.createServerHost(
        InetSocketAddress(9000),
        1500u,
        32u,
        0u,
        0u
    )
    if (serverHost == null) {
        logger.info("server host is null")
        return
    }

    var clientId = 0;
    val allClient = mutableMapOf<EnetPeer, Int>()

    while (true) {
        when (val e = serverHost.update(1u)) {
            NoneEvent -> {}
            is ConnectEvent -> {
                logger.info("a client connected with data {}", e.dataConnect)
                val existingClientId = allClient[e.peerConnect]
                if (existingClientId != null) {
                    logger.warn("impossible, client connect but had already in map???")
                } else {
                    val newClientId = clientId;
                    allClient[e.peerConnect] = newClientId;
                    clientId++;
                }
            }

            is DisconnectEvent -> {
                val id = allClient.remove(e.peerDisconnect)
                if (id != null) {
                    logger.info("client {} disconnected", id)
                } else {
                    logger.warn("impossible, client disconnected but not in map???")
                }

            }
            is ReceivePacketEvent -> {
                val clientId = allClient[e.peer]
                if (clientId != null) {
                    val buffer = e.data
                    logger.info("server received a packet from client {} size data is {}", clientId, buffer.limit())
                    val long = buffer.getLong()
                    val int = buffer.getInt()
                    logger.info("server received a packet data long {}, int {}", long, int)
                } else {
                    logger.warn("client not connect before but send packet???")
                }
            }
        }
    }

}