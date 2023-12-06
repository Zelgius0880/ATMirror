package zelgius.com.atmirror.things.repositories

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import zelgius.com.atmirror.things.entities.json.OpenWeatherMap
import java.time.Duration


class OpenWeatherMapRepository() {
    var retrofit = Retrofit.Builder()
        .client( OkHttpClient.Builder().also {
            it.readTimeout(Duration.ofSeconds(5))
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            it.addInterceptor(logger)
        }.build())
        .baseUrl("http://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var service: DarkSkyService = retrofit.create(DarkSkyService::class.java)

    interface DarkSkyService {
        //http://api.openweathermap.org/data/2.5/forecast/daily?lat=<...>&lon=<...>&cnt=5&units=metric&appid=<...>
        @GET("forecast/daily")
        fun getForecast(
            @Query("appid") key: String,
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("cnt") count: Int = 6,
            @Query("units") units: String = "metric"
        ): Call<OpenWeatherMap>
    }
}