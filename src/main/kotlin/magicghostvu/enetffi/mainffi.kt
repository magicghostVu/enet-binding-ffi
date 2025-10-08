package magicghostvu.enetffi

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer

fun main() {
    System.setProperty("log4j.configurationFile", "./log4j2.xml")
    val logger = LoggerFactory.getLogger("main")
    val resInit = Enet.init()
    logger.info("res init enet is {}", resInit)


    val buffer = ByteBuffer.allocate(1000)

    buffer.putLong(10L)
    buffer.flip()

    // cap là số được allocate từ đầu
    // limit chính là write index
    // position là read index

    logger.info("cap {}, limit {}, position {}", buffer.capacity(), buffer.limit(), buffer.position())

    /*val enetHost = Enet.createServerHost(
        InetSocketAddress(8907),
        2000u,
        32u,
        0u,
        0u
    )*/
    val enetHost = Enet.createClientHost(
        32u,
        InetSocketAddress("localhost", 7888),
        0u,
        0u,
        10
    )


    if (enetHost == null) {
        logger.error("enet host created failed")
        return
    }


    val serverPeer = enetHost.serverPeer

    repeat(15000) {
        when (val e = enetHost.update(1u)) {
            NoneEvent -> {}
            is ConnectEvent -> {
                logger.info("client connect, data send is {}", e.dataConnect)
                val dataToSend = ByteBuffer.allocate(100);
                dataToSend.putLong(10L)
                dataToSend.putInt(1000)
                dataToSend.flip()
                val resSend = serverPeer.sendData(dataToSend, 0, 0u)
                logger.info("res send to server is {}", resSend)
            }

            is DisconnectEvent -> {
                logger.info("client disconnected")
            }

            is ReceivePacketEvent -> {
                logger.info("received packet from channel {}, data len is {}", e.channelId, e.data.capacity())

                // send back data
                val buffer = ByteBuffer.allocate(8)
                buffer.putLong(125678L)
                buffer.flip()
                val resSend = e.peer.sendData(
                    buffer.array(),
                    buffer.position(),
                    buffer.limit(),
                    0,
                    0u
                )
                logger.info("res send is {}", resSend)
            }
        }
    }


}