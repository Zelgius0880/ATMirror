package zelgius.com.atmirror.shared.repository.lights

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
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
import kotlin.coroutines.CoroutineContext


object HueService : LightService {

    private val firstPartName = "ATMirror"
    private var client = OkHttpClient()

    private const val testIp = "http://10.0.2.2:3000"
    private val retrofit = {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        with(GsonBuilder()) {
            this.registerTypeAdapter(HueResponse::class.java, HueResponseConvert())

            Retrofit.Builder()
                .baseUrl(if (BuildConfig.HUE_TESTS) testIp else "https://$ip")
                .addConverterFactory(GsonConverterFactory.create(create())).apply {
                    //if (BuildConfig.HUE_TESTS)
                    client(UnsafeOkHttpClient.getUnsafeOkHttpClient(interceptor))
                }
                .build()
        }
    }

    private val gson = Gson()
    private var ip: String? = null

    private var service = retrofit().create(
        HueServiceInterface::class.java
    )

    private suspend fun getIp(): String? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://discovery.meethue.com/")
                .build()

            try {
                return@withContext client.newCall(request).execute().let {
                    if (!it.isSuccessful) return@let null

                    with(it.body!!.string()) {
                        ip = gson.fromJson(this, Array<HueDiscoverResult>::class.java)
                            .first()
                            .internalIpAddress

                        service = retrofit().create(
                            HueServiceInterface::class.java
                        )

                        ip
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    suspend fun connect(name: String): HueError? =
        withContextError(Dispatchers.IO) {
            if (ip == null) getIp()
            println(ip)

            val checkResult = service.checkRegistering(
                name
            ).execute().body()?.result?.error
            println(checkResult)

            checkResult
        }

    suspend fun register(deviceType: String = "Android"): String? =
        withContext(Dispatchers.IO) {
            try {
                if (ip == null) getIp()
                val registerResult =
                    service.sendRegisteringRequest(
                        HueRegisterRequest(
                            "${firstPartName}#$deviceType"
                        )
                    ).execute().body()
                        ?.result
                println(registerResult)

                if (registerResult?.success?.get("username") != null)
                    registerResult.success["username"]
                else null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }


    override suspend fun getLightList(name: String?): List<Light>? =
        withContext(Dispatchers.IO) {
            if (name == null) throw error("name must not be null")
            try {
                with(
                    service.getLightList(
                        name
                    ).execute()
                ) {
                    if (!isSuccessful) null
                    else {
                        body()?.list?.map {
                            Light(
                                name = it.name,
                                uid = it.id,
                                type = it.type,
                                productName = it.productName
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun setLightState(light: Light, state: Light.State, name: String?): Boolean {
        if (name == null) throw error("name must not be null")
        if (ip == null) getIp()
        return when (state) {
            Light.State.ON -> turnOnOffLight(name, light.id, true)
            Light.State.OFF -> turnOnOffLight(name, light.id, false)
            Light.State.TOGGLE -> toggleLight(name, light.id)
        }?.success?.isNotEmpty() ?: false
    }

    private suspend fun turnOnOffLight(name: String, lightNumber: String, on: Boolean): HueResult? =
        withContext(Dispatchers.IO) {
            service.setLightState(
                name,
                lightNumber,
                HueLightState(on = on)
            ).execute()
                .body()?.firstOrNull()
        }

    private suspend fun toggleLight(name: String, lightNumber: String): HueResult? {
        val state = getLightState(name, lightNumber)

        if (state != null) {
            return turnOnOffLight(name, lightNumber, state.on != true)
        }
        return null
    }

    suspend fun getLightState(name: String, lightNumber: String): HueLightState? =
        withContext(Dispatchers.IO) {
            try {
                service.getLightState(
                    name
                    , lightNumber
                ).execute()
                    .body()?.state
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
            HueError(type = 0, address = ip ?: "???", description = e.message ?: "unknown")
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
    fun checkRegistering(@Path("name") user: String?): Call<HueResponse>

    @POST("api")
    fun sendRegisteringRequest(@Body request: HueRegisterRequest): Call<HueResponse>

    @GET("api/{userName}/lights")
    fun getLightList(@Path("userName") userName: String): Call<HueResponse>

    @PUT("/api/{userName}/lights/{lightNumber}/state")
    fun setLightState(
        @Path("userName") userName: String,
        @Path("lightNumber") lightNumber: String,
        @Body state: HueLightState
    ): Call<List<HueResult>>

    @GET("/api/{userName}/lights/{lightNumber}")
    fun getLightState(
        @Path("userName") userName: String,
        @Path("lightNumber") lightNumber: String
    ): Call<HueLightStateResponse>
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
    ): HueResponse? {
        val gson = Gson()
        return if (json.isJsonArray) {
            HueResponse(result = gson.fromJson(json.asJsonArray[0], HueResult::class.java))
        } else {
            with(json.asJsonObject) {
                var id = 1
                val list = mutableListOf<HueLight>()
                while (has("$id")) {
                    list.add(gson.fromJson(getAsJsonObject("$id"), HueLight::class.java).also {
                        it.id = "$id"
                    })
                    ++id
                }

                HueResponse(list = list)
            }

        }

    }

}