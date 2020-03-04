package zelgius.com.atmirror.shared.repositories

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zelgius.com.atmirror.shared.entities.json.DarkSky


object DarkSkyRepository {
    var retrofit = Retrofit.Builder()
        .baseUrl("https://api.darksky.net")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var service = retrofit.create<DarkSkyService>(DarkSkyRepository.DarkSkyService::class.java)

    interface DarkSkyService {
        //https://api.darksky.net/forecast/[key]/[latitude],[longitude]
        @GET("forecast/{key}/{latitude},{longitude}")
        fun getForecast(
            @Path("key") key: String,
            @Path("latitude") latitude: Double,
            @Path("longitude") longitude: Double,
            @Query("lang") lang: String = "fr",
            @Query("units") units: String = "si"
        ) : Call<DarkSky>
    }
}