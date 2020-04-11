package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group(
    var name: String = "",

    @get:DocumentId
    @set:DocumentId
    override var key: String? = null,

    @get:Exclude
    @set:Exclude
    var items: List<GroupItem> = listOf()

) : FirebaseObject {

    @get:Exclude
    override val firebasePath = FIREBASE_PATH


    @get:Exclude
    val switches: List<Switch>
        get() = items
            .filter { it.itemType == GroupItem.ItemType.SWITCH }
            .map { it as Switch }

    @get:Exclude
    val lights: List<Light>
        get() = items
            .filter { it.itemType == GroupItem.ItemType.LIGHT }
            .map { it as Light }

    companion object {
        const val FIREBASE_PATH = "groups"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Group) return false
        return name == other.name
                && lights.containsAll(other.lights)
                && switches.containsAll(other.switches)
                && other.lights.containsAll(lights)
                && other.switches.containsAll(switches)
    }
}