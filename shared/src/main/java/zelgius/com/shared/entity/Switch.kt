package zelgius.com.shared.entity

import zelgius.com.utils.toHexString

data class Switch (
    val uid: String,
    var name: String = "",
    val group: Group? =null
) {

    constructor(
                 uid: ByteArray,
                 name: String = "",
                 group: Group? = null
    ) : this (uid.toHexString(), name, group)

    override fun equals(other: Any?): Boolean {
        return other is Switch && uid == other.uid
    }
}