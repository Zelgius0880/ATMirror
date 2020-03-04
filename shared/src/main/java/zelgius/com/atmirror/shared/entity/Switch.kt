package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import zelgius.com.utils.toHexString

@IgnoreExtraProperties
data class Switch (
    val uid: String,
    var name: String = ""

): FirebaseObject {

    constructor() : this("")

    @get:DocumentId
    @set:DocumentId
    override var key: String? = null

    @get:Exclude
    override val firebasePath
        get() = if (group != null) {
            String.format("${Group.FIREBASE_PATH}/${group!!.key!!}/${FIREBASE_PATH}")
        } else FIREBASE_PATH

    @get:Exclude
    @set:Exclude
    var group: Group? =null

    constructor(
                 uid: ByteArray,
                 name: String = "",
                 group: Group? = null
    ) : this (uid.toHexString(), name)


    override fun equals(other: Any?): Boolean {
        return other is Switch && uid == other.uid
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (group?.hashCode() ?: 0)
        result = 31 * result + key.hashCode()
        result = 31 * result + firebasePath.hashCode()
        return result
    }

    companion object {
        const val FIREBASE_PATH = "switches"
    }
}