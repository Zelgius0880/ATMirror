package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import zelgius.com.lights.repository.ILight

@IgnoreExtraProperties
data class Light(
    override val name: String,
    override val uid: String,
    override val id: String = uid,
    @get:Exclude
    @set:Exclude
    override var type: ILight.Type,
    @get:Exclude
    @set:Exclude
    var state: State = State.TOGGLE
) : ILight, FirebaseObject {
    constructor() : this(name = "", uid = "", type = ILight.Type.HUE)

    @get:DocumentId
    @set:DocumentId
    override var key: String? = null

    @get:Exclude
    @set:Exclude
    var group: Group? = null

    @get:Exclude
    override val firebasePath
        get() = if (group != null) {
            String.format("${Group.FIREBASE_PATH}/${group!!.key!!}/$FIREBASE_PATH")
        } else FIREBASE_PATH

    @get:Exclude
    @set:Exclude
    override var isOn: Boolean = false

    @get:PropertyName("type")
    @set:PropertyName("type")
    var typeString: String
    get() = type.name
    set(value) {
        type = ILight.Type.valueOf(value)
    }


    @get:PropertyName("state")
    @set:PropertyName("state")
    var stateString: String
        get() = state.name
        set(value) {
            state = State.valueOf(value)
        }

    enum class State {
        ON, OFF, TOGGLE
    }

    companion object {
        const val FIREBASE_PATH = "lights"
    }
}