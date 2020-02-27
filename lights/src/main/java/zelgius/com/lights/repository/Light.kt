package zelgius.com.lights.repository

import com.google.gson.annotations.SerializedName
import zelgius.com.lights.service.HueLightState

interface Light {
    val isOn: Boolean
    val name: String
    val type: String
    val lightType: Type
    val uid: String
    val id: String

    enum class Type {
        HUE, LIFX
    }

    enum class Action {
        TOGGLE, ON, OFF
    }
}

data class HueLight(
    @SerializedName("number") override var id: String,
    val state: HueLightState,
    override val name: String,
    override val type: String,
    @SerializedName("uniqueid") val uniqueId:  String
) : Light {
    override val isOn: Boolean
        get() = state.on?:false

    override val lightType: Light.Type
        get() = Light.Type.HUE

    override val uid: String
        get() = uniqueId
}

data class LIFXLight(
    override val id: String,
    @SerializedName("uuid")override val uid: String,
    val power: String,
    @SerializedName("label") override val name: String,
    val product: Product
    ) : Light {

    override val lightType: Light.Type
        get() = Light.Type.LIFX

    override val isOn: Boolean
        get() = power == "on"

    override val type: String
        get() = product.name

    data class Product(
        val name: String,
        val identifier: String
    )
}