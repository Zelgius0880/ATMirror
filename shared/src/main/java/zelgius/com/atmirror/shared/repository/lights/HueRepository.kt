package zelgius.com.atmirror.shared.repository.lights

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import zelgius.com.atmirror.shared.BuildConfig
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.repository.LightService
import zelgius.com.lights.repository.HueLight
import zelgius.com.lights.repository.UnsafeOkHttpClient
import java.io.IOException
import java.lang.reflect.Type
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext


object HueService : LightService {

    private const val FIRST_PART_NAME = "ATMirror"

    private fun retrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return with(GsonBuilder()) {
            this.registerTypeAdapter(HueResponse::class.java, HueResponseConvert())

            Retrofit.Builder()
                .baseUrl("https://$ip")
                .addConverterFactory(GsonConverterFactory.create(create())).apply {
                    //if (BuildConfig.HUE_TESTS)
                    client(UnsafeOkHttpClient.getUnsafeOkHttpClient(interceptor))
                }
                .build()
        }
    }

    private var ip: String = BuildConfig.HUE_DEFAULT_IP

    private var _service: HueServiceInterface? = null

    private val service: HueServiceInterface
        get() = _service ?: retrofit().create(
            HueServiceInterface::class.java
        ).apply {
            _service = this
        }

    suspend fun connect(name: String): HueError? =
        withContextError(Dispatchers.IO) {
            val checkResult = service.checkRegistering(
                name
            ).result?.error
            println(checkResult)

            checkResult
        }

    suspend fun register(deviceType: String = "Android"): String? =
        withContext(Dispatchers.IO) {
            try {
                val registerResult =
                    service.sendRegisteringRequest(
                        HueRegisterRequest(
                            "${FIRST_PART_NAME}#$deviceType"
                        )
                    ).result
                println(registerResult)

                if (registerResult?.success?.get("username") != null)
                    registerResult.success["username"]
                else null
            } catch (e: UnknownHostException) {
                _service = null
                null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }


    override suspend fun getLightList(name: String?): List<Light>? {
        if (name == null) error("name must not be null")

        return try {
            service.getLightList(
                name
            ).let { response ->
                response.list?.map {
                    Light(
                        name = it.name,
                        uid = it.id,
                        type = it.type,
                        productName = it.productName
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun setLightState(
        vararg light: Light,
        state: Light.State,
        name: String?
    ): Boolean {
        if (name == null) error("name must not be null")
        return when (state) {
            Light.State.ON -> light.map {
                turnOnOffLight(name, it.id, true, it.hue, it.saturation, it.brightness)
            }.last()

            Light.State.OFF -> light.map {
                turnOnOffLight(name, it.id, false, it.hue, it.saturation, it.brightness)
            }.last()

            Light.State.TOGGLE -> toggleLight(*light, name = name)
        }?.success?.isNotEmpty() ?: false
    }

    private suspend fun turnOnOffLight(
        name: String,
        lightNumber: String,
        on: Boolean,
        hue: Int?,
        saturation: Int?,
        brightness: Int?
    ): HueResult? =
        try {
            service.setLightState(
                name,
                lightNumber,
                HueLightState(
                    on = on,
                    hue = hue,
                    saturation = saturation?.toShort(),
                    brightness = brightness?.toShort()
                )
            ).firstOrNull()
        } catch (e: UnknownHostException) {
            _service = null
            null
        }


    private suspend fun toggleLight(vararg light: Light, name: String): HueResult? {
        val firstLight = light.first()
        val state = getLightState(name, firstLight.id)

        if (state != null) {
            return light.map {
                turnOnOffLight(name, it.id, state.on != true, it.hue, it.saturation, it.brightness)
            }.last()
        }
        return null
    }

    suspend fun getLightState(name: String, lightNumber: String): HueLightState? =
        withContext(Dispatchers.IO) {
            try {
                service.getLightState(
                    name, lightNumber
                ).state
            } catch (e: UnknownHostException) {
                _service = null
                null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }


    private suspend fun withContextError(
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> HueError?
    ): HueError? = withContext(context) {
        try {
            block()
        } catch (e: IOException) {
            HueError(type = 0, address = ip, description = e.message ?: "unknown")
        }
    }
}

data class HueDiscoverResult(
    val id: String,
    @SerializedName("internalipaddress") val internalIpAddress: String
)

data class HueError(val type: Int, val address: String, val description: String)
data class HueResult(val error: HueError?, val success: Map<String, String>?)
data class HueRegisterRequest(
    @SerializedName("devicetype") val deviceType: String
)

data class HueLightState(
    val on: Boolean? = null,
    @SerializedName("bri") val brightness: Short? = null, // 0 - 255
    @SerializedName("sat") val saturation: Short? = null, // 0 - 255
    val hue: Int? = null// 0 - 65535
)

interface HueServiceInterface {
    @GET("api/{name}")
    suspend fun checkRegistering(@Path("name") user: String?): HueResponse

    @POST("api")
    suspend fun sendRegisteringRequest(@Body request: HueRegisterRequest): HueResponse

    @GET("api/{userName}/lights")
    suspend fun getLightList(@Path("userName") userName: String): HueResponse

    @PUT("/api/{userName}/lights/{lightNumber}/state")
    suspend fun setLightState(
        @Path("userName") userName: String,
        @Path("lightNumber") lightNumber: String,
        @Body state: HueLightState
    ): List<HueResult>

    @GET("clip/v2/resource/light/{lightNumber}")
    suspend fun getLightState(
        @Header("hue-application-key") key: String,
        @Path("lightNumber") lightNumber: String
    ): HueLightStateResponse
}

data class HueLightStateResponse(val state: HueLightState)

data class HueResponse(
    val result: HueResult? = null,
    val list: List<HueLight>? = null
)

class HueResponseConvert : JsonDeserializer<HueResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): HueResponse {
        val gson = Gson()
        return if (json.isJsonArray) {
            HueResponse(result = gson.fromJson(json.asJsonArray[0], HueResult::class.java))
        } else {
            val list = mutableListOf<HueLight>()
            with(json.asJsonObject) {
                keySet().forEach { id ->
                    list.add(gson.fromJson(getAsJsonObject(id), HueLight::class.java).also {
                        it.id = id
                    })
                }

                HueResponse(list = list)
            }
        }

    }
}