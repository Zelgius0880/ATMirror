package zelgius.com.atmirror.entities.json

import com.google.gson.annotations.SerializedName


data class OpenWeatherMap(
    val city: City = City(),
    val list: List<ForecastData> = mutableListOf(),
    val message: String = ""
) {
    constructor() : this(City())
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpenWeatherMap

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }
}

data class ForecastData(
    @SerializedName("dt") var time: Long = 0L,
    val sunrise: Long = 0L,
    val sunset: Long = 0L,
    val temp: Temperature = Temperature(),
    @SerializedName("feels_like") var feelsLike: Temperature = Temperature(),
    val pressure: Double = 0.0,
    val humidity: Double = 0.0,
    val speed: Double = 0.0,
    val deg: Double = 0.0,
    val clouds: Double = 0.0,
    val pop: Double = 0.0,
    val rain: Double = 0.0,
    val snow: Double = 0.0,
    val weather: List<Weather> = listOf()
) {
    constructor() : this(0L)
}

data class Weather(
    val id: Int = 0,
    val main: String = "",
    val description: String ="",
    val icon: String =""
){
    constructor() : this(0)
}

data class Temperature(
    val min: Double?,
    val max: Double? = null,
    val day: Double = 0.0,
    val night: Double = 0.0,
    val eve: Double = 0.0,
    val morn: Double = 0.0
){
    constructor(): this(null)
}

data class City(
    val id: Long = 0,
    val name: String = "",
    val country: String = "",
    val population: Int = 0,
    val timezone: Long = 0,
    val coord: Coordinate = Coordinate()
){
    constructor() : this(0)
}

data class Coordinate(
    val lon: Double  = 0.0,
    val lat: Double = 0.0
){
    constructor() : this(0.0)
}
