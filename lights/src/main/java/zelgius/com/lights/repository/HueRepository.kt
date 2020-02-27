package zelgius.com.lights.service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import zelgius.com.lights.repository.HueLight
import zelgius.com.lights.repository.UnsafeOkHttpClient


object HueService {

    private val firstPartName = HueService::class.java.`package`!!.name
    var name = "defaultName"
    private var client = OkHttpClient()

    var test = false
    private const val testIp = "http://127.0.0.1:3000"
    private val retrofit by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        Retrofit.Builder()
            .baseUrl(if (test) testIp else "https://$ip")
            .addConverterFactory(GsonConverterFactory.create())
            .client(UnsafeOkHttpClient.getUnsafeOkHttpClient(interceptor))
            .build()
    }

    private val hueName: String
        get() = "$firstPartName#$name"

    private val gson = Gson()
    var ip: String? = null

    private lateinit var mUserName: String

    val userName
        get() = mUserName

    private val service by lazy { retrofit.create(HueServiceInterface::class.java) }

    suspend fun getIp(): String? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://discovery.meethue.com/")
                .build()

            try {
                return@withContext client.newCall(request).execute().let {
                    if (!it.isSuccessful) return@let null

                    with(it.body!!.string()) {
                        gson.fromJson(this, Array<HueDiscoverResult>::class.java)
                            .first()
                            .internalIpAddress
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    suspend fun connect(): HueError? =
        withContext(Dispatchers.IO) {
            if (ip == null) ip = getIp()
            println(ip)

            val checkResult = service.checkRegistering(firstPartName).execute().body()?.first()
            println(checkResult)

            checkResult?.error
        }

    suspend fun register(): HueError? =
        withContext(Dispatchers.IO) {
            val registerResult =
                service.sendRegisteringRequest(HueRegisterRequest(hueName)).execute().body()
                    ?.first()
            println(registerResult)

            if (registerResult?.success?.entries?.first()?.value != null)
                mUserName = registerResult.success?.entries?.first()?.value!!

            registerResult?.error
        }


    suspend fun getLightList(): List<HueLight> =
        withContext(Dispatchers.IO) {
            /*val type = object : TypeToken<HashMap<String, HueLight>>() {}.type
            val clonedMap: HashMap<Int, HueLight> = gson.fromJson(jsonString, type)*/

            service.getLightList(userName).execute().body()
                ?.map {
                    it.value.id = it.key
                    it.value
                }?: listOf()
        }

    suspend fun setLightStatus(lightNumber: String, state: HueLightState): List<HueResult> =
        withContext(Dispatchers.IO) {
            service.setLightState(userName, lightNumber, state).execute()
                .body()?: listOf()
        }

    suspend fun turnOnLight(lightNumber: String, on: Boolean): HueResult? =
        withContext(Dispatchers.IO) {
            service.setLightState(userName, lightNumber, HueLightState(on = on)).execute()
                .body()?.firstOrNull()
        }

}

data class HueDiscoverResult(val id: String, @SerializedName("internalipaddress") val internalIpAddress: String)
data class HueError(val type: Int, val address: String, val description: String)
data class HueResult(val error: HueError?, val success: Map<String, String>?)
data class HueRegisterRequest(@SerializedName("devicetype") val deviceType: String)

data class HueLightState(
    val on: Boolean? = null,
    @SerializedName("bri") val brightness: Short? = null, // 0 - 255
    @SerializedName("sat") val saturation: Short? = null, // 0 - 255
    val hue: Int? = null// 0 - 65535
)

interface HueServiceInterface {
    @GET("api/{name}")
    fun checkRegistering(@Path("name") user: String?): Call<List<HueResult>>

    @POST("api")
    fun sendRegisteringRequest(@Body request: HueRegisterRequest): Call<List<HueResult>>

    @GET("api/{userName}/lights")
    fun getLightList (@Path("userName") userName: String): Call<Map<String, HueLight>>

    @PUT("/api/{userName}/lights/{lightNumber}/state")
    fun setLightState(@Path("userName") userName: String,
                      @Path("lightNumber") lightNumber: String,
                      @Body state: HueLightState ) : Call<List<HueResult>>
}