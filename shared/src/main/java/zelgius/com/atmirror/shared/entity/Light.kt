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
    var state: State = State.TOGGLE,
    @get:Exclude
    override val productName: String = "",
    @get:Exclude
    @set:Exclude
    public override  var group: Group? = null,
    @get:DocumentId
    @set:DocumentId
    override var key: String? = null
) : ILight, GroupItem(uid, ItemType.LIGHT) {
    constructor() : this(name = "", uid = "", type = ILight.Type.HUE)




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



    override fun toString(): String {
        return "Light(name='$name', uid='$uid', id='$id', type=$type, state=$state, key=$key)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Light

        if (name != other.name) return false
        if (uid != other.uid) return false
        if (id != other.id) return false
        if (type != other.type) return false
        if (state != other.state) return false
        if (key != other.key) return false

        return group?.key == other.key
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        return result
    }


}