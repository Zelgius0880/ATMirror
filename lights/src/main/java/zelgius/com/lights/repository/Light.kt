package zelgius.com.lights.repository

import com.google.gson.annotations.SerializedName
import zelgius.com.lights.service.HueLightState

interface ILight {
    val isOn: Boolean
    val name: String
    val type: Type
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
    @SerializedName("uniqueid") val uniqueId:  String
) : ILight {
    override val isOn: Boolean
        get() = state.on?:false

    override val type: ILight.Type
        get() = ILight.Type.HUE

    override val uid: String
        get() = uniqueId
}

data class LIFXLight(
    override val id: String,
    @SerializedName("uuid")override val uid: String,
    val power: String,
    @SerializedName("label") override val name: String,
    val product: Product
    ) : ILight {

    override val type: ILight.Type
        get() = ILight.Type.LIFX

    override val isOn: Boolean
        get() = power == "on"

    data class Product(
        val name: String,
        val identifier: String
    )
}