package zelgius.com.atmirror.shared.protocol

import java.lang.IllegalStateException

class CurrentStatus: Protocol {
    override val code: Code =
        Code.CURRENT_STATUS

    enum class Status(val code: Byte) {
        NOT_WORKING(0x00),
        SWITCH_DISCOVERING(0x01)
    }

    val status: Status

    constructor(status: Status = Status.NOT_WORKING): super(
        Code.CURRENT_STATUS.size) {
        rawPayload[0] = status.code
        this.status = status
    }

    constructor(bytes: ByteArray) : super(bytes) {
        status = when(val v = rawPayload[0].toInt()) {
            0x00 -> Status.NOT_WORKING
            0x01 -> Status.SWITCH_DISCOVERING
            else -> throw IllegalStateException("Unknown status: $v")
        }
    }


}