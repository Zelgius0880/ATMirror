package zelgius.com.lights.repository

import com.google.gson.annotations.SerializedName
import zelgius.com.atmirror.shared.repository.lights.HueLightState

interface ILight {
    val isOn: Boolean
    val name: String
    val type: Type
    val productName: String
    val uid: String
    val id: String

    enum class Type {
        HUE, LIFX
    }
}

data class HueLight(
    @SerializedName("number") override var id: String,
    val state: HueLightState,
    override val name: String,
    @SerializedName("uniqueid") val uniqueId:  String,
    @SerializedName("type") override val productName: String
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

    override val productName: String
        get() = product.name

    override val type: ILight.Type
        get() = ILight.Type.LIFX

    override val isOn: Boolean
        get() = power == "on"

    data class Product(
        val name: String,
        val identifier: String
    )
}