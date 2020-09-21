package zelgius.com.atmirror.repositories

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import zelgius.com.atmirror.BuildConfig
import zelgius.com.utils.toLocalDateTime
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class NetatmoRepository(val debug: Boolean = false) {
    val url = "https://api.netatmo.com/api/getstationsdata?device_id={mac}&get_favorites=false"

    private var retrofit = Retrofit.Builder()
        .baseUrl("https://api.netatmo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .apply {
            if (debug)
                client(
                    OkHttpClient.Builder().also {
                        val logger = HttpLoggingInterceptor()
                        logger.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(logger)
                    }.build()

                )
        }
        .build()

    private var service: NetatmoService = retrofit.create(NetatmoService::class.java)

    companion object {

        private var token: Token? = null
    }

    suspend fun getTemperatureMeasure(module: Boolean= false) = withToken {
        withContext(Dispatchers.IO) {
            service.getTemperatureMeasure(token = "Bearer $it",
                moduleId = if(module) BuildConfig.NETATMO_MODULE_MAC else null,
                dateEnd = LocalDateTime.now().plusHours(1).toEpochSecond(OffsetDateTime.now().offset)
            ).execute()
                .body()?.let {
                    it.body.map {e -> e.key.toLong().toLocalDateTime() to
                            (e.value.maxOrNull()?: Double.MAX_VALUE) }
                }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun requestToken() =
        withContext(Dispatchers.IO) {
            service.getToken().execute()
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun refreshToken() =
        withContext(Dispatchers.IO) {
            token?.let {
                service.refreshToken(refreshToken = it.refreshToken).execute()
            } ?: requestToken()
        }

    suspend fun <T> withToken(block: suspend (token: String) -> T): T =
        block(token.let {
            when {
                it == null -> with(requestToken()) {
                    if (isSuccessful) token = body()
                    token?.accessToken ?: error("Get access token failed")
                }

                !it.isValid -> with(refreshToken()) {
                    if (isSuccessful) token = body()
                    token?.accessToken ?: error("Get refresh token failed")
                }

                else -> token?.accessToken ?: error("Damn, how did you come there?!")
            }
        })


    interface NetatmoService {
        //http://api.openweathermap.org/data/2.5/forecast/daily?lat=<...>&lon=<...>&cnt=5&units=metric&appid=<...>
        @GET("api/getmeasure")
        fun getTemperatureMeasure(
            @Header("Authorization") token: String,
            @Query("device_id") deviceId: String = BuildConfig.NETATMO_MAC,
            @Query("module_id") moduleId: String? = null,
            @Query("scale") scale: String = "30min",
            @Query("type") type: String = "temperature",
            @Query("date_end") dateEnd: Long = LocalDateTime.now()
                .toEpochSecond(OffsetDateTime.now().offset),
            @Query("date_begin") dateBegin: Long = LocalDateTime.now()
                .minusHours(1)
                .toEpochSecond(OffsetDateTime.now().offset),
            @Query("optimize") optimize: Boolean = false,
            @Query("real_time") realTime: Boolean = true,
        ): Call<MeasureResponse>

        @POST("oauth2/token")
        @FormUrlEncoded()
        fun getToken(
            @Field("grant_type") grantType: String = "password",
            @Field("scope") scope: String = "read_station",
            @Field("client_id") clientId: String = BuildConfig.NETATMO_CLIENT_ID,
            @Field("client_secret") clientSecret: String = BuildConfig.NETATMO_CLIENT_SECRET,
            @Field("username") userName: String = BuildConfig.NETATMO_USER_EMAIL,
            @Field("password") password: String = BuildConfig.NETATMO_USER_PASSWORD
        ): Call<Token>

        @POST("oauth2/token")
        @FormUrlEncoded
        fun refreshToken(
            @Field("grant_type") grantType: String = "refresh_token",
            @Field("refresh_token") refreshToken: String,
            @Field("client_id") clientId: String = BuildConfig.NETATMO_CLIENT_ID,
            @Field("client_secret") clientSecret: String = BuildConfig.NETATMO_CLIENT_SECRET
        ): Call<Token>
    }

    data class Token(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("expires_in") val expiresIn: Long,
        @SerializedName("refresh_token") val refreshToken: String
    ) {

        private var creationDate: LocalDateTime = LocalDateTime.now()

        constructor()  : this("", 0L ,""){
           creationDate =  LocalDateTime.now()
        }

        val isValid
            get() =  LocalDateTime.now().isBefore(
                creationDate.plusSeconds(expiresIn)
            )
    }

    data class MeasureResponse(
        val body: Map<String, List<Double>>
    )
}