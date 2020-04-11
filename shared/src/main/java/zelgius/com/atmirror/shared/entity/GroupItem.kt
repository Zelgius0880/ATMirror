package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
abstract class GroupItem(
    open val uid: String,
    val itemType: ItemType
): FirebaseObject {

    @get:Exclude
    @set:Exclude
    protected open var group: Group? = null

    enum class ItemType{
        SWITCH, LIGHT
    }

    companion object {
        const val FIREBASE_PATH = "items"
    }
}