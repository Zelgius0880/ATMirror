package zelgius.com.atmirror.shared.repository.lights

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread


object LIFXLanService {

    const val PROTOCOL = 0b0000010000000000
    const val TAGGED = 0b0011000000000000
    const val NOT_TAGGED = 0b0001000000000000
    const val SOURCE = 0x00000FD42
    const val HEADER_SIZE = 36

    var listener: ((data: LIFXPacket) -> Unit)? = null

    private val socket = DatagramSocket(56700).apply {
        //broadcast = true
    }

    private var mRunning = false
    val running
        get() = mRunning

    suspend fun sendPacket(packet: LIFXPacket) {
        withContext(Dispatchers.IO) {
            val socket = DatagramSocket()
            socket.broadcast = true

            val buffer = packet.bytes()

            println("Sending ${buffer.toHex()}")
            val datagramPacket =
                DatagramPacket(buffer, buffer.size, InetAddress.getByName("255.255.255.255"), 56700)
            socket.send(datagramPacket)
            socket.close()
        }
    }

    @TestOnly
    suspend fun sendPacket(buffer: ByteArray) {
        withContext(Dispatchers.IO) {
            val socket = DatagramSocket()
            socket.broadcast = true

            println("Sending ${buffer.toHex()}")
            val datagramPacket =
                DatagramPacket(buffer, buffer.size, InetAddress.getByName("255.255.255.255"), 56700)
            socket.send(datagramPacket)
            socket.close()
        }
    }

    fun startListening() {
        val receiveData = ByteArray(1024)
        val localIp = InetAddress.getLocalHost().hostAddress.toString()
        thread {
            try {
                mRunning = true
                while (true) {
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    socket.receive(receivePacket)

                    if (!receivePacket.address.toString().contains(localIp)) {
                        val ipAddress = receivePacket.address
                        val port = receivePacket.port
                        println("$ipAddress $port RECEIVED: " + receiveData.toHex())

                        listener?.invoke(
                            LIFXPacket.build(
                                receiveData
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mRunning = false
            }
        }
    }

    fun close() {
        socket.close()
    }
}

data class LIFXPacket(
    val command: Command,
    val address: Long? = null,
    val sequence: Int? = null,

    // payload
    var power: Short? = null,
    var duration: Int? = null,
    var service: Byte? = null,
    var port: Int? = null,
    var color: Int? = null, // HSBK
    var label: String? = null // HSBK
) {
    /**
     * b0
     * b1 size
     * b2
     * b3 header
     * b4
     * b5
     * b6
     * b7 source
     * b8
     * b9
     * b10
     * b11
     * b12
     * b13
     * b14
     * b15 target
     * b16
     * b17
     * b18
     * b19
     * b20
     * b21 reserved
     * b22 ack required
     * b23 sequence
     * b24
     * b25
     * b26
     * b27
     * b28
     * b29
     * b30
     * b31 protocol header (reserved)
     * b32
     * b33 command
     * b34
     * b35 reserved
     */
    fun bytes(): ByteArray {
        val payload = when (command) {
            Command.GET, Command.GET_SERVICE -> byteArrayOf()
            Command.POWER -> {
                ByteBuffer.allocate(6)
                    .apply {
                        order(ByteOrder.LITTLE_ENDIAN)
                        putShort(power!!)
                        putInt(duration!!)
                    }.array()
            }

            else -> throw IllegalStateException("Unknown send command")
        }

        val size =
            LIFXLanService.HEADER_SIZE - (if (payload.isEmpty()) 2 else 0) + payload.size

        return ByteBuffer.allocate(size).apply {
            order(ByteOrder.LITTLE_ENDIAN)

            putShort(size.toShort())// size
            putShort((LIFXLanService.PROTOCOL or if (address != null) LIFXLanService.NOT_TAGGED else LIFXLanService.TAGGED).toShort())// header
            putInt(LIFXLanService.SOURCE)// source
            putLong(address ?: 0L)// target
            putInt(0)
            putShort(0.toShort())// reserved
            put((if (sequence != null) 0x01 else 0x00).toByte())// ack required
            put((sequence ?: 0).toByte())// sequence
            putLong(0L)// protocol header (reserved)
            putShort(command.value)// command

            if (payload.isNotEmpty()) {
                putShort(0.toShort()) // reserved
                put(payload)
            }

        }.array()

    }

    enum class Command(val value: Short) {
        // get the state
        // [no payload]
        GET(101.toShort()),

        // set power
        // [power][power][duration][duration][duration][duration] -> power is 0 - 65635, duration is in milli
        POWER(117.toShort()),

        //get service
        // [no payload]
        GET_SERVICE(2.toShort()),


        //state service
        // [service][port][port][port][port]
        STATE_SERVICE(3.toShort()),

        //state
        //[h][h][s][s][b][b][k][k][reserved][reserved][power][power][label]x32 [reserved][reserved][reserved][reserved]
        STATE(107)
        ;

        companion object {
            fun parse(cmd: Short) = when (cmd) {
                GET.value -> GET
                STATE_SERVICE.value -> STATE_SERVICE
                GET_SERVICE.value -> GET_SERVICE

                else -> throw IllegalStateException("Unknown command: $cmd")
            }
        }
    }

    companion object {
        fun build(data: ByteArray): LIFXPacket =
            with(ByteBuffer.wrap(data)) {
                order(ByteOrder.LITTLE_ENDIAN)

                val size = short// size
                short // header -> not significant for response
                val source = int// source
                val target = long// target
                int // reserved
                short// reserved
                get()// ack required
                val sequence = get()// sequence
                long// protocol header (reserved)
                val command = short
                short //reserved

                val packet =
                    LIFXPacket(
                        address = target,
                        command = Command.parse(
                            command
                        ),
                        sequence = sequence.toInt()
                    )

                when (packet.command) {
                    Command.STATE_SERVICE -> {
                        packet.service = get()
                        packet.port = int
                    }

                    Command.STATE -> {
                        packet.color = int
                        short // reserved
                        packet.power = short
                        val label = ByteArray(32)
                        get(label)
                        packet.label = String(label)

                    }

                    else -> throw IllegalStateException("Unknown receive command: ${packet.command}")
                }

                packet
            }

    }
}

fun ByteArray.toHex(): String {
    val hexArray = "0123456789ABCDEF".toCharArray()
    val hexChars = CharArray(size * 2)
    for (j in 0 until size) {
        val v: Int = get(j).toInt() and 0xFF
        hexChars[j * 2] = hexArray[v ushr 4]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}
