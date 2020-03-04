package zelgius.com.atmirror.shared.protocol

import zelgius.com.utils.hexStringToByteArray
import zelgius.com.utils.toHexString

class NewSwitch: Protocol {

    override val code: Code =
        Code.NEW_SWITCH
    val uid
        get() = rawPayload

    val uidString
        get() = uid.toHexString()

    constructor(hex: String) : super(hex.length / 2) { // HEX string to byte array size
        hex.hexStringToByteArray().forEachIndexed { index, value ->
            rawPayload[index] = value
        }
    }

    constructor(switchCode: ByteArray) : super(switchCode.size ) {
        switchCode.forEachIndexed { index, value ->
            rawPayload[index] = value
        }
    }
}