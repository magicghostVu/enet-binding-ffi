package magicghostvu.enetffi

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object Enet {


    @JvmStatic
    private val funcInit: MethodHandle

    @JvmStatic
    private val funcDeInit: MethodHandle


    @JvmStatic
    private val funcDestroyHost: MethodHandle


    @JvmStatic
    private val funcSetHostAddress: MethodHandle

    @JvmStatic
    private val funcCreateHost: MethodHandle

    @JvmStatic
    // this method need to be inlined because it will be called very frequently
    private val funcHostService: MethodHandle


    private val funcEnetMalloc: MethodHandle

    private val funEnetFree: MethodHandle

    private val funcDestroyEnetPacket: MethodHandle

    private val funcCreateEnetPacketWithFreeCallback: MethodHandle

    private val funcEnetPeerSend: MethodHandle

    private val funcHostConnect: MethodHandle


    private val funcDisconnectPeer: MethodHandle

    private val funcDisconnectPeerLater: MethodHandle


    private const val NUM_MAX_CLIENTS = 4095u
    private const val NUM_MAX_CHANNEL_PER_CLIENTS = 255u


    internal val logger: Logger = LoggerFactory.getLogger("enet")

    init {

        // todo: maybe move to resource
        val filePath = File("enet_dll/enet_1.3.17.dll")
        System.load(filePath.absolutePath);
        // load some function

        val libLookup = SymbolLookup.loaderLookup()
        val enetInitAdd = libLookup.find("enet_initialize").orElseThrow()
        val nativeLinker = Linker.nativeLinker()
        funcInit = nativeLinker.downcallHandle(
            enetInitAdd,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT
            )
        )

        val enetDeInitAdd = libLookup.find("enet_deinitialize").orElseThrow()
        funcDeInit = nativeLinker.downcallHandle(
            enetDeInitAdd,
            FunctionDescriptor.ofVoid()
        )


        val funcDestroyHostAdd = libLookup.find("enet_host_destroy").orElseThrow()
        funcDestroyHost = nativeLinker.downcallHandle(
            funcDestroyHostAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS
            )
        )


        val funSetHostAddressAdd = libLookup.find("enet_address_set_host").orElseThrow()

        funcSetHostAddress = nativeLinker.downcallHandle(
            funSetHostAddressAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS
            )
        )

        val funcCreateHostAdd = libLookup.find("enet_host_create").orElseThrow()
        funcCreateHost = nativeLinker.downcallHandle(
            funcCreateHostAdd,
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
            )
        )


        val funcHostServiceAdd = libLookup.find("enet_host_service").orElseThrow()

        funcHostService = nativeLinker.downcallHandle(
            funcHostServiceAdd,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
            )
        )

        val funcEnetMallocAdd = libLookup.find("enet_malloc").orElseThrow()
        funcEnetMalloc = nativeLinker.downcallHandle(
            funcEnetMallocAdd,
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
            )
        )

        val funEnetFreeAdd = libLookup.find("enet_free").orElseThrow()

        funEnetFree = nativeLinker.downcallHandle(
            funEnetFreeAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
            )
        )


        val funcEnetPacketDestroyAdd = libLookup.find("enet_packet_destroy").orElseThrow()
        funcDestroyEnetPacket = nativeLinker.downcallHandle(
            funcEnetPacketDestroyAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
            )
        )

        val funcCreateEnetPacketWithFreeCallbackAdd =
            libLookup.find("enet_packet_create_with_free_callback").orElseThrow()
        funcCreateEnetPacketWithFreeCallback = nativeLinker.downcallHandle(
            funcCreateEnetPacketWithFreeCallbackAdd,
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT,
            )
        )

        val funcPeerSendAdd = libLookup.find("enet_peer_send").orElseThrow()

        funcEnetPeerSend = nativeLinker.downcallHandle(
            funcPeerSendAdd,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_BYTE,
                ValueLayout.ADDRESS,
            )
        )

        val funcHostConnectAdd = libLookup.find("enet_host_connect").orElseThrow()
        funcHostConnect = nativeLinker.downcallHandle(
            funcHostConnectAdd,
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT,
            )
        )

        val funcDisconnectPeerAdd = libLookup.find("enet_peer_disconnect").orElseThrow()
        funcDisconnectPeer = nativeLinker.downcallHandle(
            funcDisconnectPeerAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT
            )
        )

        val funcDisconnectPeerLaterAdd = libLookup.find("enet_peer_disconnect_later").orElseThrow()

        funcDisconnectPeerLater = nativeLinker.downcallHandle(
            funcDisconnectPeerLaterAdd,
            FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
            )
        )

    }


    fun init(): Int {
        return funcInit.invokeExact() as Int
    }

    fun deinit() {
        funcDeInit.invokeExact()
    }


    fun createServerHost(
        addressToBind: InetSocketAddress,
        maxNumClient: UInt,
        maxNumChannelPerClient: UInt,
        incomingBandwidthBytesPerSecond: UInt,
        outgoingBandwidthBytesPerSecond: UInt
    ): EnetServerHost? {

        if (maxNumClient > NUM_MAX_CLIENTS) {
            throw IllegalArgumentException("maxNumClient (value $maxNumClient) > $NUM_MAX_CLIENTS")
        }

        if (maxNumChannelPerClient > NUM_MAX_CHANNEL_PER_CLIENTS) {
            throw IllegalArgumentException("maxNumChannel (value $maxNumChannelPerClient) > $NUM_MAX_CHANNEL_PER_CLIENTS")
        }

        val structEnetAddress = EnetStructLayout.structEnetAddress

        val arena = Arena.ofConfined()
        val enetHostAddress = arena.use {
            val addressPointer = it.allocate(structEnetAddress.layout)
            val handleSetPort = structEnetAddress.handlePort
            handleSetPort.set(addressPointer, 0, addressToBind.port.toShort())
            val cPointerStringHost = it.allocateFrom(addressToBind.hostName, StandardCharsets.UTF_8)

            funcSetHostAddress.invokeExact(
                addressPointer, cPointerStringHost,
            )
            funcCreateHost.invokeExact(
                addressPointer,
                maxNumClient.toLong(),
                maxNumChannelPerClient.toLong(),
                incomingBandwidthBytesPerSecond.toInt(),
                outgoingBandwidthBytesPerSecond.toInt(),
            ) as MemorySegment
        }
        return if (enetHostAddress == MemorySegment.NULL) null
        else EnetServerHost(enetHostAddress);
    }

    internal fun destroyHost(host: EnetHost) {
        funcDestroyHost.invokeExact(host.hostAddress)
    }

    internal fun createEnetEvent(): MemorySegment {
        return funcEnetMalloc.invokeExact(EnetStructLayout.structEnetEvent.byteSize) as MemorySegment
    }

    internal fun freeEnetEvent(pointerEnetEvent: MemorySegment) {
        funEnetFree.invokeExact(pointerEnetEvent)
    }

    fun createClientHost(
        numChannel: UByte,
        targetConnect: InetSocketAddress,
        maxBandwidthInBytePerSecond: UInt,
        maxBandwidthOutBytePerSecond: UInt,
        dataConnect: Int
    ): EnetClientHost? {

        if (numChannel > NUM_MAX_CHANNEL_PER_CLIENTS) {
            throw IllegalArgumentException("num channel can not bigger than $NUM_MAX_CHANNEL_PER_CLIENTS")
        }
        val clientHostAdd = funcCreateHost.invokeExact(
            MemorySegment.NULL,
            1L,
            numChannel.toLong(),
            maxBandwidthInBytePerSecond.toInt(),
            maxBandwidthOutBytePerSecond.toInt()
        ) as MemorySegment

        return if (clientHostAdd == MemorySegment.NULL) null
        else {
            // call host connect
            val arena = Arena.ofConfined()
            val serverPeerAllocated = arena.use {
                val addressToConnectPointer = it.allocate(EnetStructLayout.structEnetAddress.layout)
                val cStringHost = it.allocateFrom(targetConnect.hostName, StandardCharsets.UTF_8)
                funcSetHostAddress.invokeExact(addressToConnectPointer, cStringHost)
                EnetStructLayout.structEnetAddress.handlePort.set(
                    addressToConnectPointer, 0, targetConnect.port.toShort(),
                )
                funcHostConnect.invokeExact(
                    clientHostAdd,
                    addressToConnectPointer,
                    numChannel.toLong(),
                    dataConnect
                ) as MemorySegment
            }

            return if (serverPeerAllocated == MemorySegment.NULL) {
                funcDestroyHost.invokeExact(clientHostAdd)
                null
            } else {
                EnetClientHost(
                    clientHostAdd,
                    EnetPeer(serverPeerAllocated)
                )
            }
        }
    }


    internal fun hostService(host: EnetHost, delayMs: UInt): EnetEvent {
        val nativeEventToPass = host.reuseEventPointer

        // call host service
        val resUpdate = funcHostService.invokeExact(host.hostAddress, nativeEventToPass, delayMs.toInt()) as Int;
        if (resUpdate < 0) {
            throw RuntimeException("update return err code $resUpdate")
        }

        val enetStructEvent = EnetStructLayout.structEnetEvent

        val varHandleReadType = enetStructEvent.varHandleReadType

        //todo: reinterpret at create, so no need resize again?
        val resizedEvent = nativeEventToPass.reinterpret(enetStructEvent.byteSize)


        val typeId = varHandleReadType.get(resizedEvent, 0) as Int
        val eventType = EnetEventType.fromCode(typeId)


        return when (eventType) {
            EnetEventType.ENET_EVENT_TYPE_NONE -> NoneEvent
            EnetEventType.ENET_EVENT_TYPE_CONNECT -> {

                val handleGetData = enetStructEvent.handleGetData
                val data = handleGetData.get(resizedEvent, 0) as Int


                val handleGetPeerAddress = enetStructEvent.handleGetPeerAddress
                val peerAddress = handleGetPeerAddress.get(resizedEvent, 0) as MemorySegment
                ConnectEvent(
                    EnetPeer(peerAddress),
                    data
                )
            }

            EnetEventType.ENET_EVENT_TYPE_DISCONNECT -> {
                val handleGetData = enetStructEvent.handleGetData
                val data = handleGetData.get(resizedEvent, 0) as Int
                val handleGetPeerAddress = enetStructEvent.handleGetPeerAddress
                val peerAddress = handleGetPeerAddress.get(resizedEvent, 0) as MemorySegment
                DisconnectEvent(
                    EnetPeer(peerAddress),
                    data
                )
            }

            EnetEventType.ENET_EVENT_TYPE_RECEIVE -> {
                val handleGetPeerAddress = enetStructEvent.handleGetPeerAddress
                val peerAddress = handleGetPeerAddress.get(resizedEvent, 0) as MemorySegment
                val enetPeer = EnetPeer(peerAddress)

                val handleGetChannelId = enetStructEvent.handleGetChannelId
                val channelId = (handleGetChannelId.get(resizedEvent, 0) as Byte).toUByte()
                val handleGetPacketAddress = enetStructEvent.handleGetPacketAddress
                val enetPacketLayout = EnetStructLayout.structEnetPacket.layout

                val nativeEnetPacketAddress = (handleGetPacketAddress.get(resizedEvent, 0) as MemorySegment)
                    .reinterpret(enetPacketLayout.byteSize())

                val enetPacketStruct = EnetStructLayout.structEnetPacket
                val packetFlagsHandle = enetPacketStruct.packetFlagsHandle
                val flags = packetFlagsHandle.get(nativeEnetPacketAddress, 0) as Int
                val dataLengthHandle = enetPacketStruct.dataLengthHandle
                val dataLength = (dataLengthHandle.get(nativeEnetPacketAddress, 0) as Long).toInt()
                val nativeDataBufferHandle = enetPacketStruct.nativeDataBufferHandle
                val nativeDataBuffer = (nativeDataBufferHandle.get(nativeEnetPacketAddress, 0) as MemorySegment)
                    .reinterpret(dataLength.toLong())
                    .asByteBuffer()

                // allocate heap byte array to copy data from native side
                val heapBuffer = ByteBuffer.allocate(dataLength)

                // do copy
                heapBuffer.put(nativeDataBuffer)

                // convert to read mode
                heapBuffer.flip()

                //todo: destroy native packet to free memory
                funcDestroyEnetPacket.invokeExact(nativeEnetPacketAddress) as Unit

                ReceivePacketEvent(
                    enetPeer,
                    channelId,
                    heapBuffer,
                    flags
                )
            }
        }
    }


    internal fun disconnectPeer(enetPeer: EnetPeer, data: Int) {
        funcDisconnectPeer.invokeExact(enetPeer.nativeAddress, data)
    }

    internal fun disconnectPeerLater(enetPeer: EnetPeer, data: Int) {
        funcDisconnectPeerLater.invokeExact(enetPeer.nativeAddress, data)
    }

    // netty buffer and jdk bytebuffer friendly
    internal fun peerSend(
        peer: EnetPeer,
        data: ByteArray,
        offset: UInt,
        dataLength: UInt,
        flags: Int,
        channelId: UByte
    ): Int {

        // create native buffer
        val nativeBufferAddress = funcEnetMalloc.invokeExact(dataLength.toLong()) as MemorySegment
        val nativeDataBuffer = nativeBufferAddress
            .reinterpret(dataLength.toLong())
            .asByteBuffer()

        // copy data from heap to this native buffer
        nativeDataBuffer.put(data, offset.toInt(), dataLength.toInt())

        val enetPacket = funcCreateEnetPacketWithFreeCallback.invokeExact(
            nativeBufferAddress,
            dataLength.toLong(),
            flags
        ) as MemorySegment


        val resSend = funcEnetPeerSend.invokeExact(
            peer.nativeAddress,
            channelId.toByte(),
            enetPacket,
        ) as Int

        if (resSend < 0) {
            //destroy packet
            funcDestroyEnetPacket.invokeExact(enetPacket) as Unit
        }

        return resSend
    }

}