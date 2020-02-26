package zelgius.com.shared.entities

abstract class Lamp(val name: String, val uid: String) {
    abstract val type: Type
    enum class Type{HUE, LIFIX}
}