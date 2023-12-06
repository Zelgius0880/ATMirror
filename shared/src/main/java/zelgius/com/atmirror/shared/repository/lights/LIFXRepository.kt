package zelgius.com.atmirror.shared.repository.lights

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
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.repository.LightService
import zelgius.com.lights.repository.LIFXLight
import java.io.IOException


object LIFXService : LightService {
    var token = ""
    private val retrofit by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder();

        httpClient.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token")
                        .build();
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
    private val service by lazy {
        retrofit.create(
            LIFXServiceInterface::class.java
        )
    }


    private suspend fun getLightList(): List<LIFXLight>? =
        withContext(Dispatchers.IO) {
            try {
                with(service.getLightList().execute()) {
                    if (!isSuccessful) null
                    else body() ?: listOf()
                }
            } catch (e: IOException) {
                null
            }

        }

    override suspend fun setLightState(vararg light: Light, state: Light.State, name: String?) = light.flatMap {
        when (state) {
            Light.State.ON -> setLightState(it.id, "on")
            Light.State.OFF -> setLightState(it.id, "off")
            Light.State.TOGGLE -> toggleLightState(it.id)
        }
    }.firstOrNull()?.status == "ok"


    override suspend fun getLightList(name: String?) =
        getLightList()?.map {
            Light(
                name = it.name,
                uid = it.id,
                type = it.type,
                productName = it.productName
            )
        }

    private suspend fun setLightState(
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

    private suspend fun toggleLightState(
        id: String
    ): List<LIFXResult> =
        withContext(Dispatchers.IO) {
            service.toggle(id).execute()
                .body()?.results ?: listOf()
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

        @POST("lights/id:{id}/toggle")
        fun toggle(@Path("id") id: String): Call<LIFXResponse>
    }

    data class LIFXResponse(val results: List<LIFXResult>)
    data class LIFXResult(
        val id: String,
        val status: String,
        val label: String
    )
}