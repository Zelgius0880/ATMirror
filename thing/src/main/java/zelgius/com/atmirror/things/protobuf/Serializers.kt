package zelgius.com.atmirror.things.protobuf

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import zelgius.com.atmirror.shared.repository.MeasureType
import zelgius.com.atmirror.shared.repository.NetatmoResult
import zelgius.com.atmirror.things.entities.json.ForecastData
import zelgius.com.atmirror.things.entities.json.OpenWeatherMap
import zelgius.com.atmirror.things.entities.json.Temperature
import zelgius.com.atmirror.things.entities.json.Weather
import zelgius.com.atmirror.things.proto.ForecastProto
import zelgius.com.atmirror.things.proto.OpenWeatherMapProto
import zelgius.com.utils.toLocalDateTime
import java.io.InputStream
import java.io.OutputStream
import java.time.ZoneOffset


object ForecastSerializer : Serializer<OpenWeatherMapProto> {
    override val defaultValue: OpenWeatherMapProto = OpenWeatherMapProto.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): OpenWeatherMapProto {
        try {
            return OpenWeatherMapProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: OpenWeatherMapProto, output: OutputStream) {
        t.writeTo(output)
    }
}

object NetatmoResultSerializer : Serializer<NetatmoResultProto> {
    override val defaultValue: NetatmoResultProto = NetatmoResultProto.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): NetatmoResultProto {
        try {
            return NetatmoResultProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: NetatmoResultProto, output: OutputStream) {
        t.writeTo(output)
    }
}

fun NetatmoResultProto.toEntity() =
    entriesList.groupBy { it.measure }
        .mapKeys { (key, _) -> MeasureType.values().first { it.measure == key } }
        .mapValues { (_, value) ->
            value.map { NetatmoResult((it.time * 1000).toLocalDateTime(), it.value) }
        }


fun Map<MeasureType, List<NetatmoResult>>.toProto() = NetatmoResultProto.newBuilder()
    .addAllEntries(
        flatMap { (key, value) ->
            value.map {
                NetatmoResultProto.Entry.newBuilder()
                    .setTime(it.time.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .setValue(it.data)
                    .setMeasure(key.measure)
                    .build()
            }
        })
    .build()

fun ForecastProto.Temperature.toEntity() = Temperature(
    min = min,
    max = max,
    day = day,
    night = night,
    eve = eve,
    morn = morn,
)

fun ForecastProto.Weather.toEntity() = Weather(
    id = id,
    main = main,
    description = description,
    icon = icon,
)

fun ForecastProto.toEntity() = ForecastData(
    time = time,
    sunrise = sunrise,
    sunset = sunset,
    temp = temp.toEntity(),
    feelsLike = feelsLike.toEntity(),
    pressure = pressure,
    humidity = humidity,
    speed = speed,
    deg = deg,
    clouds = clouds,
    pop = pop,
    rain = rain,
    snow = snow,
    weather = weatherList.map { it.toEntity() },
)


fun Temperature.toProto() = ForecastProto.Temperature.newBuilder().also {
    it.min = min ?: 0.0
    it.max = max ?: 0.0
    it.day = day
    it.night = night
    it.eve = eve
    it.morn = morn
}.build()

fun Weather.toProto() = ForecastProto.Weather.newBuilder().also {
    it.id = id
    it.main = main
    it.description = description
    it.icon = icon
}.build()

fun ForecastData.toProto() = ForecastProto.newBuilder().also {
    it.time = time
    it.sunrise = sunrise
    it.sunset = sunset
    it.temp = temp.toProto()
    it.feelsLike = feelsLike.toProto()
    it.pressure = pressure
    it.humidity = humidity
    it.speed = speed
    it.deg = deg
    it.clouds = clouds
    it.pop = pop
    it.rain = rain
    it.snow = snow

    it.addAllWeather(weather.map { it.toProto() })
}.build()

fun OpenWeatherMap.toProto() = OpenWeatherMapProto.newBuilder()
    .addAllForecast(list.map { it.toProto() })
    .build()