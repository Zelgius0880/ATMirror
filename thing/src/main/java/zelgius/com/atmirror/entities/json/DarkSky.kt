package zelgius.com.atmirror.entities.json

class DarkSky(
    var latitude: Double,
    var longitude: Double,
    var timezone: String,
    var daily: Forecast,
    var currently: Forecast,
    var alerts: List<Alert>
) {
    constructor() : this(0.0, 0.0, "", Forecast(), Forecast(), mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DarkSky

        if (currently != other.currently) return false

        return true
    }

    override fun hashCode(): Int {
        return currently.hashCode()
    }

}

data class Forecast(
    var summary: String,
    var icon: String,
    var data: List<ForecastData>
) {
    constructor() : this("", "", mutableListOf())


}

class ForecastData(
    var time: Long = 0L,
    var summary: String,
    var icon: String,
    var sunriseTime: Long,
    var sunsetTime: Long,
    var moonPhase: Double,
    var precipIntensity: Double,
    var precipIntensityMax: Double,
    var precipIntensityMaxTime: Long,
    var precipProbability: Double,
    var precipType: String,
    var temperatureHigh: Double,
    var temperatureHighTime: Long,
    var temperatureLow: Double,
    var temperatureLowTime: Long,
    var apparentTemperatureHigh: Double,
    var apparentTemperatureHighTime: Long,
    var apparentTemperatureLow: Double,
    var apparentTemperatureLowTime: Long,
    var dewPoint: Double,
    var humidity: Double,
    var pressure: Double,
    var windSpeed: Double,
    var windGust: Double,
    var windGustTime: Long,
    var windBearing: Double,
    var cloudCover: Double,
    var uvIndex: Double,
    var uvIndexTime: Long,
    var visibility: Double,
    var ozone: Double,
    var temperatureMin: Double,
    var temperatureMinTime: Long,
    var temperatureMax: Double,
    var temperatureMaxTime: Long,
    var apparentTemperatureMin: Double,
    var apparentTemperatureMinTime: Long,
    var apparentTemperatureMax: Double,
    var apparentTemperatureMaxTime: Long
) {
    constructor() : this(
        0L,
        "",
        "",
        0L,
        0L,
        0.0,
        0.0,
        0.0,
        0L,
        0.0,
        "",
        0.0,
        0L,
        0.0,
        0L,
        0.0,
        0L,
        0.0,
        0L,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0L,
        0.0,
        0.0,
        0.0,
        0L,
        0.0,
        0.0,
        0.0,
        0L,
        0.0,
        0L,
        0.0,
        0L,
        0.0,
        0L
    )
}

data class Alert(
    var title: String,
    var time: Long,
    var expires: Long,
    var description: String
) {
    constructor() : this("", 0L, 0L, "")
}