package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import zelgius.com.atmirror.shared.BuildConfig
import zelgius.com.atmirror.shared.entity.FirebaseObject
import zelgius.com.utils.toLocalDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime

class NetatmoRepository(val debug: Boolean = false) : FirebaseRepository() {
    val url = "https://api.netatmo.com/api/getstationsdata?device_id={mac}&get_favorites=false"

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var retrofit = Retrofit.Builder()
        .baseUrl("https://api.netatmo.com/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build()
        )
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().apply {
            setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes): Boolean =
                    f.getAnnotation(Exclude::class.java) != null

                override fun shouldSkipClass(clazz: Class<*>?): Boolean = false

            })
        }.create()))
        .apply {
            if (debug)
                client(
                    OkHttpClient.Builder().also {
                        it.readTimeout(Duration.ofSeconds(5))
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


    suspend fun getMeasure(
        module: Boolean = false,
        dateEnd: LocalDateTime = LocalDateTime.now(),
        dateBegin: LocalDateTime = LocalDateTime.now()
            .minusHours(1),
        vararg measure: MeasureType = arrayOf(MeasureType.Temperature)
    ): Map<MeasureType, List<NetatmoResult>> = withToken {
        withContext(Dispatchers.IO) {
            service.getMeasure(
                token = "Bearer $it",
                type = measure.joinToString(separator = ",") { it.measure },
                moduleId = if (module) BuildConfig.NETATMO_MODULE_MAC else null,
                dateEnd = dateEnd.toEpochSecond(OffsetDateTime.now().offset),
                dateBegin = dateBegin.toEpochSecond(OffsetDateTime.now().offset)
            ).body()?.body?.let { result ->
                measure.mapIndexed { index, measureType ->
                    measureType to result.map {
                        NetatmoResult(it.key.toLong().toLocalDateTime(), it.value[index])
                    }
                }.toMap()
            } ?: mapOf()
        }
    }


    suspend fun requestAndSaveToken(code: String) {
        withContext(Dispatchers.IO) {
            service.getToken(code = code).body()?.let {
                createOrUpdate(
                    TokenElement(
                        firebasePath = "states",
                        key = "token",
                        refreshToken = it.refreshToken,
                        accessToken = it.accessToken,
                        expiresIn = it.expiresIn
                    ), "states"
                )
            }
        }
    }

    @IgnoreExtraProperties
    data class TokenElement(
        @get:com.google.firebase.firestore.Exclude
        override val firebasePath: String = "",
        @get:com.google.firebase.firestore.Exclude
        @set:com.google.firebase.firestore.Exclude
        override var key: String?,
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long
    ) : FirebaseObject


    init {
        scope.launch {
            listen(
                path = "states",
                key = "token"
            ) { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                documentSnapshot?.toObject(Token::class.java)?.let {
                    token = it
                }
            }
        }
    }


    suspend fun refreshToken() =
        withContext(Dispatchers.IO) {
            token?.let {
                service.refreshToken(refreshToken = it.refreshToken)
            }
        }

    suspend fun <T> withToken(block: suspend (token: String) -> T): T =
        block(token.let {
            if (token == null) {
                token = getSnapshot(path = "states", key = "token").toObject(Token::class.java)
            } else if (token?.isValid == false) {
                refreshToken()?.let {
                    if (it.isSuccessful) {
                        token = it.body()?.also {
                            createOrUpdate(
                                TokenElement(
                                    firebasePath = "states",
                                    key = "token",
                                    refreshToken = it.refreshToken,
                                    accessToken = it.accessToken,
                                    expiresIn = it.expiresIn
                                ), "states"
                            )
                        }
                    }
                }
            }

            token?.accessToken ?: ""
        })


    interface NetatmoService {
        //http://api.openweathermap.org/data/2.5/forecast/daily?lat=<...>&lon=<...>&cnt=5&units=metric&appid=<...>
        @GET("api/getmeasure")
        suspend fun getMeasure(
            @Header("Authorization") token: String,
            @Query("device_id") deviceId: String = BuildConfig.NETATMO_MAC,
            @Query("module_id") moduleId: String? = null,
            @Query("scale") scale: String = "30min",
            @Query("date_end") dateEnd: Long = LocalDateTime.now()
                .toEpochSecond(OffsetDateTime.now().offset),
            @Query("date_begin") dateBegin: Long = LocalDateTime.now()
                .minusHours(1)
                .toEpochSecond(OffsetDateTime.now().offset),
            @Query("optimize") optimize: Boolean = false,
            @Query("real_time") realTime: Boolean = true,
            @Query("type") type: String = "temperature"
        ): Response<MeasureResponse>

        @POST("oauth2/token")
        @FormUrlEncoded
        suspend fun getToken(
            @Field("grant_type") grantType: String = "authorization_code",
            @Field("scope") scope: String = "read_station",
            @Field("code") code: String,
            @Field("redirect_uri") redirectUri: String = "atmirror://token-result",
            @Field("client_id") clientId: String = BuildConfig.NETATMO_CLIENT_ID,
            @Field("client_secret") clientSecret: String = BuildConfig.NETATMO_CLIENT_SECRET,
        ): Response<Token>

        @POST("oauth2/token")
        @FormUrlEncoded
        suspend fun refreshToken(
            @Field("grant_type") grantType: String = "refresh_token",
            @Field("refresh_token") refreshToken: String,
            @Field("client_id") clientId: String = BuildConfig.NETATMO_CLIENT_ID,
            @Field("client_secret") clientSecret: String = BuildConfig.NETATMO_CLIENT_SECRET
        ): Response<Token>
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Exclude
    data class Token(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("expires_in") val expiresIn: Long,
        @SerializedName("refresh_token") val refreshToken: String
    ) {

        @Exclude
        private var creationDate: LocalDateTime = LocalDateTime.now()

        constructor() : this("", 0L, "") {
            creationDate = LocalDateTime.now()
        }

        val isValid
            get() = LocalDateTime.now().isBefore(
                creationDate.plusSeconds(expiresIn)
            )
    }

    data class MeasureResponse(
        val body: Map<String, List<Double>>
    )
}

enum class MeasureType(val measure: String) {
    Temperature("temperature"), Humidity("humidity"), Pressure("pressure")
}

data class NetatmoResult(
    val time: LocalDateTime,
    val data: Double
)
