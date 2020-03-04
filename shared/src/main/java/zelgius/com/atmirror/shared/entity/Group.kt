package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Group (
    var name: String = "",
    @get:Exclude
    @set:Exclude
    var switches: List<Switch> = listOf(),

    @get:Exclude
    @set:Exclude
    var lights: List<Light> = listOf()

): FirebaseObject {

    @get:DocumentId
    @set:DocumentId
    override var key: String? = null

    @get:Exclude
    override val firebasePath = FIREBASE_PATH

    companion object {
        const val FIREBASE_PATH = "groups"
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Group) return false
        return name == other.name
                && lights.containsAll(other.lights)
                && switches.containsAll(other.switches)
                && other.lights.containsAll(lights)
                && other.switches.containsAll(switches)
    }
}