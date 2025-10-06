package magicghostvu.enetffi

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer

fun main() {
    System.setProperty("log4j.configurationFile", "./log4j2.xml")
    val logger = LoggerFactory.getLogger("main")
    val resInit = Enet.init()
    logger.info("res init enet is {}", resInit)
    val enetHost = Enet.createServerHost(
        InetSocketAddress(8907),
        2000u,
        32u,
        0u,
        0u
    )
    if (enetHost == null) {
        logger.error("enet host created failed")
        return
    }

    repeat(15000) {
        when (val e = enetHost.update(1u)) {
            NoneEvent -> {}
            is ConnectEvent -> {
                logger.info("client connect, data send is {}", e.dataConnect)
            }

            is DisconnectEvent -> {
                logger.info("client disconnected")
            }

            is ReceivePacketEvent -> {
                logger.info("received packet from channel {}, data len is {}", e.channelId, e.data.capacity())

                // send back data
                val dataLength = 8
                val buffer = ByteBuffer.allocate(8)
                buffer.putLong(125678L)
                buffer.flip()
                val resSend = e.peer.sendData(
                    buffer.array(),
                    buffer.position(),
                    dataLength,
                    0,
                    0u
                )
                logger.info("res send is {}", resSend)
            }
        }
    }


}