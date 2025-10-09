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

    while (true) {
        when (val e = serverHost.update(1u)) {
            is ConnectEvent -> {
                logger.info("a client connected with data {}", e.dataConnect)
            }

            is DisconnectEvent -> {
                logger.info("a client disconnected ")
            }

            NoneEvent -> {}
            is ReceivePacketEvent -> {
                val buffer = e.data
                logger.info("server received a packet size data is {}", buffer.limit())

                val long = buffer.getLong()
                val int = buffer.getInt()

                logger.info("server received a packet data long {}, int {}", long, int)

            }
        }
    }

}