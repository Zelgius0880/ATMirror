package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import zelgius.com.utils.toHexString

@IgnoreExtraProperties
data class Switch (
    override val uid: String,
    var name: String = "",

    @get:Exclude
    @set:Exclude
    public override var group: Group? = null,

    @get:DocumentId
    @set:DocumentId
    override var key: String? = null
): GroupItem(uid, ItemType.SWITCH) {

    constructor() : this("")


    @get:Exclude
    override val firebasePath
        get() = if (group != null) {
            String.format("${Group.FIREBASE_PATH}/${group!!.key!!}/${FIREBASE_PATH}")
        } else FIREBASE_PATH

    constructor(
                 uid: ByteArray,
                 name: String = "",
                 group: Group? = null
    ) : this (removeDuplicateEntries(uid).toHexString(), name)


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

    override fun toString(): String {
        return "Switch(uid='$uid', name='$name', key=$key)"
    }


    companion object {
        private fun removeDuplicateEntries(array: ByteArray): ByteArray {
            var repeating =
                false // will be true if dividing the array by divider will give the same subsequence
            // ex: [abcabcabc] -> [abc] [abc] [abc]

            var divider = 1

            do {
                ++divider
                if (array.size % divider == 0) {
                    for (i in 0 until array.size / divider) {
                        for (j in 1 until divider) {
                            repeating = array[i] == array[j * array.size / divider + i]
                        }
                    }
                }
            } while (divider != array.size && !repeating)

            return if (repeating)
                array.sliceArray(0 until array.size / divider)
            else array
        }
    }
}