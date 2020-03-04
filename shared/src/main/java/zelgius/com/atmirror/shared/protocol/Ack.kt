package zelgius.com.atmirror.shared.protocol

class Ack: Protocol {
    override val code: Code =
        Code.ACK
    val ackOk: Boolean


    constructor(ackOk: Boolean = true): super(ByteArray(Code.ACK.size)) {
        rawPayload[0] = if(ackOk) 0x00 else 0x01
        this.ackOk = ackOk
    }

    constructor(bytes: ByteArray) : super(bytes) {
        ackOk = rawPayload[0] == 0x00.toByte()
    }


}