package zelgius.com.shared.entities.protocol

import java.lang.IllegalStateException
import java.nio.ByteBuffer

abstract class Protocol(val size: Int) {
    abstract val code: Code
    protected val rawPayload = ByteArray(size)

    enum class Code(val number: Byte, val size: Int) {
        ACK(0x00, 1),
        START_DISCOVERY(0x01, 0),
        NEW_SWITCH(0x01, -1),
        STOP_DISCOVERY(0x02, 0),
        GET_CURRENT_STATUS(0x03, 0),
        CURRENT_STATUS(0x03, 1),
    }


    /**
     * Byte array size should be at least 2
     */
    constructor(bytes: ByteArray) : this(bytes[1].toInt()) {
        if (size > 0)
            (0..size).forEach {
                rawPayload[it] = bytes[it + 2]
            }
    }

    fun build(): ByteArray {
        val buffer = ByteBuffer.allocate(rawPayload.size + 2) // payload + protocol byte + size byte
        buffer.put(code.number)
        buffer.put(rawPayload.size.toByte())
        buffer.put(rawPayload)

        return buffer.array()
    }

    companion object {
        fun parse(bytes: ByteArray) =
            when (bytes[0]) {
                Code.ACK.number -> Ack(bytes)
                Code.STOP_DISCOVERY.number, Code.NEW_SWITCH.number -> if (bytes[1] > 0) NewSwitch(
                    bytes
                ) else StartDiscovery()
                Code.STOP_DISCOVERY.number -> StopDiscovery()
                Code.GET_CURRENT_STATUS.number, Code.CURRENT_STATUS.number -> if (bytes[1] > 0) CurrentStatus(
                    bytes
                ) else GetCurrentStatus()
                else -> throw IllegalStateException("Protocol unknown: ${bytes[0]}")
            }
    }
}