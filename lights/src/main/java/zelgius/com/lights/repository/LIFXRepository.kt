package zelgius.com.lights.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


object LIFXService {
    var token = ""
    private val retrofit by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder();

        httpClient.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build();
                return chain.proceed(request);
            }
        });

        httpClient.addInterceptor(interceptor)

        Retrofit.Builder()
            .baseUrl("https://api.lifx.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
    }
    private val service by lazy { retrofit.create(LIFXServiceInterface::class.java) }


    suspend fun getLightList(): List<LIFXLight> =
        withContext(Dispatchers.IO) {
            service.getLightList().execute().body()
                ?: listOf()
        }

    suspend fun setLightState(
        id: String,
        power: String? = null,
        color: String? = null,
        brightness: Double? = null,
        duration: Double? = null, // seconds
        fast: Boolean? = null
    ): List<LIFXResult> =
        withContext(Dispatchers.IO) {
            service.setState(id, power, color, brightness, duration, fast).execute()
                .body()?.results ?: listOf()
        }


    suspend fun turnOnLight(id: String, on: Boolean): List<LIFXResult> =
        withContext(Dispatchers.IO) {
            setLightState(id = id, power = if (on) "on" else "off")
        }


    interface LIFXServiceInterface {
        @GET("lights/all")
        fun getLightList(): Call<List<LIFXLight>>


        @FormUrlEncoded
        @PUT("lights/id:{id}/state")
        fun setState(
            @Path("id") id: String,
            @Field("power") power: String? = null,
            @Field("color") color: String? = null,
            @Field("brightness") brightness: Double? = null,
            @Field("duration") duration: Double? = null, // second
            @Field("fast") fast: Boolean? = null
        ): Call<LIFXResponse>
    }

    data class LIFXResponse(val results: List<LIFXResult>)
    data class LIFXResult(
        val id: String,
        val status: String,
        val label: String
    )
}