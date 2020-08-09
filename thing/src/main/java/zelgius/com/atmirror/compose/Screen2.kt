package zelgius.com.atmirror.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.google.gson.Gson
import zelgius.com.atmirror.R
import zelgius.com.atmirror.entities.json.DarkSky
import zelgius.com.atmirror.entities.json.ForecastData
import zelgius.com.utils.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.compose.ui.graphics.Color as CColor

data class Screen2(val forecast: DarkSky) {
    var bitmap: Bitmap? = null


    constructor(forecast: DarkSky, bitmap: Bitmap) : this(forecast) {
        this.bitmap = bitmap
    }
}


@Composable
fun Screen2View(forecast: MutableState<DarkSky>) {
    Box(Modifier.size(300.dp, 400.dp)) {
        forecast.value.apply {
            if (daily.data.size >= 6) {
                (0 until 6 step 2).forEach {
                    Row {
                        ForecastCell(item = daily.data[it])
                        ForecastCell(item = daily.data[it + 1])
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastCell(item: ForecastData) {

    Column(modifier = Modifier.size(150.dp, 133.dp) then Modifier.padding(top = 4.dp)) {

        ForecastCellHeader(item)

        Divider(color = CColor.Red, modifier = Modifier.padding(horizontal = 8.dp), thickness = 2.dp)

        Stack(modifier = Modifier.height(90.dp) then Modifier.fillMaxWidth()) {
            Image(
                vectorResource(
                    id = when (item.icon) {
                        "clear-day" -> R.drawable.ic_wi_day_sunny
                        "clear-night" -> R.drawable.ic_wi_night_clear
                        "rain" -> R.drawable.ic_wi_rain
                        "snow" -> R.drawable.ic_wi_snow
                        "sleet" -> R.drawable.ic_wi_sleet
                        "wind" -> R.drawable.ic_wi_strong_wind
                        "fog" -> R.drawable.ic_wi_fog
                        "cloudy" -> R.drawable.ic_wi_cloud
                        "partly-cloudy-day" -> R.drawable.ic_wi_day_cloudy
                        "partly-cloudy-night" -> R.drawable.ic_wi_night_cloudy
                        "hail" -> R.drawable.ic_wi_hail
                        "thunderstorm" -> R.drawable.ic_wi_thunderstorm
                        "tornado" -> R.drawable.ic_wi_tornado
                        else -> R.drawable.ic_wi_na
                    }
                ),
                modifier = Modifier.fillMaxHeight() then Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(CColor.White)
            )

            Box(
                gravity = ContentGravity.TopStart,
                modifier = Modifier.fillMaxHeight() then Modifier.fillMaxWidth()
                        then Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    String.format("%.1f", item.temperatureMax), style = TextStyle(
                        fontSize = 24.sp, color = CColor.Black,
                        shadow = Shadow(
                            color = CColor.White,
                            offset = Offset(0f, 0f),
                            blurRadius = 1f
                        )

                    )
                )
            }

            Box(
                gravity = ContentGravity.BottomEnd,
                modifier = Modifier.fillMaxHeight() then Modifier.fillMaxWidth()
                        then Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    String.format("%.1f", item.temperatureMin),
                    style = TextStyle(
                        fontSize = 24.sp, color = CColor.Black,
                        shadow = Shadow(
                            color = CColor.White,
                            offset = Offset(0f, 0f),
                            blurRadius = 1f
                        )

                    )
                )
            }
        }
    }
}

@Composable
private fun ForecastCellHeader(item: ForecastData) {
    val date = (item.time * 1000).toLocalDateTime(ZoneId.of("Europe/Brussels"))
    val now = LocalDateTime.now(ZoneId.of("Europe/Brussels"))
    val day = if (now.dayOfMonth == date.dayOfMonth) {
        ContextAmbient.current.getString(R.string.today)
    } else {
        val array = ContextAmbient.current.resources.getStringArray(R.array.day_of_the_week)

        when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> array[0]
            DayOfWeek.TUESDAY -> array[1]
            DayOfWeek.WEDNESDAY -> array[2]
            DayOfWeek.THURSDAY -> array[3]
            DayOfWeek.FRIDAY -> array[4]
            DayOfWeek.SATURDAY -> array[5]
            DayOfWeek.SUNDAY -> array[6]
            else -> ""
        }

    }

    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        Text(
            text = day,
            style = TextStyle(
                fontSize = 16.sp, color = CColor.White
            )
        )

        Box(modifier = Modifier.fillMaxWidth(), gravity = ContentGravity.CenterEnd) {
            Row {
                Image(
                    modifier = Modifier.width(12.dp),
                    asset = vectorResource(id = R.drawable.ic_wi_raindrop),
                    colorFilter = ColorFilter.tint(CColor.White),
                    contentScale = ContentScale.Crop
                )

                Box(modifier = Modifier.height(30.dp), gravity = ContentGravity.CenterEnd) {
                    Text(
                        text = String.format("%.0f%%", item.precipProbability * 100),
                        style = TextStyle(
                            fontSize = 16.sp, color = CColor.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun Cell() {
    ForecastCell(Gson().fromJson(sample, DarkSky::class.java).daily.data.first())
}

@Composable
@Preview
private fun Preview() {

    //Box(Modifier.size(300.dp, 400.dp)) {
        Screen2View(state { Gson().fromJson(sample, DarkSky::class.java) })
    //}
}

const val sample = """
    {"latitude":50.09224,"longitude":5.32388,"timezone":"Europe/Brussels","currently":{"time":1596893039,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":91.86,"apparentTemperature":92,"dewPoint":61.04,"humidity":0.36,"pressure":1018.7,"windSpeed":3.71,"windGust":3.71,"windBearing":124,"cloudCover":0,"uvIndex":7,"visibility":10,"ozone":293.4},"hourly":{"summary":"Clear throughout the day.","icon":"clear-day","data":[{"time":1596891600,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":91.44,"apparentTemperature":91.66,"dewPoint":61.29,"humidity":0.37,"pressure":1018.8,"windSpeed":3.76,"windGust":3.78,"windBearing":125,"cloudCover":0,"uvIndex":7,"visibility":10,"ozone":293.4},{"time":1596895200,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":92.24,"apparentTemperature":92.26,"dewPoint":60.69,"humidity":0.35,"pressure":1018.5,"windSpeed":3.62,"windGust":3.63,"windBearing":122,"cloudCover":0,"uvIndex":6,"visibility":10,"ozone":293.4},{"time":1596898800,"summary":"Clear","icon":"clear-day","precipIntensity":0.0002,"precipProbability":0.01,"precipType":"rain","temperature":91.78,"apparentTemperature":91.78,"dewPoint":60.33,"humidity":0.35,"pressure":1018.5,"windSpeed":3.55,"windGust":3.79,"windBearing":112,"cloudCover":0,"uvIndex":4,"visibility":10,"ozone":293.3},{"time":1596902400,"summary":"Clear","icon":"clear-day","precipIntensity":0.0005,"precipProbability":0.01,"precipType":"rain","temperature":90.84,"apparentTemperature":90.93,"dewPoint":61.05,"humidity":0.37,"pressure":1018.2,"windSpeed":3.64,"windGust":4.23,"windBearing":89,"cloudCover":0,"uvIndex":2,"visibility":10,"ozone":292.6},{"time":1596906000,"summary":"Clear","icon":"clear-day","precipIntensity":0.0009,"precipProbability":0.01,"precipType":"rain","temperature":89.16,"apparentTemperature":89.94,"dewPoint":62.74,"humidity":0.42,"pressure":1018,"windSpeed":3.85,"windGust":4.76,"windBearing":70,"cloudCover":0,"uvIndex":1,"visibility":10,"ozone":291.8},{"time":1596909600,"summary":"Clear","icon":"clear-day","precipIntensity":0.0011,"precipProbability":0.01,"precipType":"rain","temperature":86.74,"apparentTemperature":87.77,"dewPoint":63.35,"humidity":0.46,"pressure":1018,"windSpeed":4.04,"windGust":5.18,"windBearing":62,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":291.3},{"time":1596913200,"summary":"Clear","icon":"clear-day","precipIntensity":0.0008,"precipProbability":0.01,"precipType":"rain","temperature":83.46,"apparentTemperature":84.62,"dewPoint":63.34,"humidity":0.51,"pressure":1018.2,"windSpeed":4.21,"windGust":5.42,"windBearing":61,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":291.7},{"time":1596916800,"summary":"Clear","icon":"clear-night","precipIntensity":0.0006,"precipProbability":0.01,"precipType":"rain","temperature":79.99,"apparentTemperature":81.38,"dewPoint":62.89,"humidity":0.56,"pressure":1018.4,"windSpeed":4.36,"windGust":5.53,"windBearing":66,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":292.5},{"time":1596920400,"summary":"Clear","icon":"clear-night","precipIntensity":0.0005,"precipProbability":0.01,"precipType":"rain","temperature":77.22,"apparentTemperature":77.48,"dewPoint":62.46,"humidity":0.6,"pressure":1018.6,"windSpeed":4.44,"windGust":5.56,"windBearing":77,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":292.8},{"time":1596924000,"summary":"Clear","icon":"clear-night","precipIntensity":0.0004,"precipProbability":0.01,"precipType":"rain","temperature":75.35,"apparentTemperature":75.6,"dewPoint":62.38,"humidity":0.64,"pressure":1018.6,"windSpeed":4.43,"windGust":5.65,"windBearing":92,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":292.3},{"time":1596927600,"summary":"Clear","icon":"clear-night","precipIntensity":0.0003,"precipProbability":0.01,"precipType":"rain","temperature":74.03,"apparentTemperature":74.28,"dewPoint":62.36,"humidity":0.67,"pressure":1018.4,"windSpeed":4.35,"windGust":5.64,"windBearing":110,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":291.4},{"time":1596931200,"summary":"Clear","icon":"clear-night","precipIntensity":0.0003,"precipProbability":0.01,"precipType":"rain","temperature":72.96,"apparentTemperature":73.16,"dewPoint":61.86,"humidity":0.68,"pressure":1018.3,"windSpeed":4.29,"windGust":5.46,"windBearing":126,"cloudCover":0,"uvIndex":0,"visibility":10,"ozone":290.6},{"time":1596934800,"summary":"Clear","icon":"clear-night","precipIntensity":0.0004,"precipProbability":0.01,"precipType":"rain","temperature":72.07,"apparentTemperature":72.14,"dewPoint":60.64,"humidity":0.67,"pressure":1018.3,"windSpeed":4.28,"windGust":4.92,"windBearing":139,"cloudCover":0.23,"uvIndex":0,"visibility":10,"ozone":290.3},{"time":1596938400,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0005,"precipProbability":0.02,"precipType":"rain","temperature":71.47,"apparentTemperature":71.47,"dewPoint":59.32,"humidity":0.66,"pressure":1018.4,"windSpeed":4.3,"windGust":4.57,"windBearing":148,"cloudCover":0.54,"uvIndex":0,"visibility":10,"ozone":290.3},{"time":1596942000,"summary":"Mostly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0005,"precipProbability":0.02,"precipType":"rain","temperature":70.88,"apparentTemperature":70.88,"dewPoint":58.52,"humidity":0.65,"pressure":1018.4,"windSpeed":4.39,"windGust":4.46,"windBearing":153,"cloudCover":0.76,"uvIndex":0,"visibility":10,"ozone":290},{"time":1596945600,"summary":"Mostly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0004,"precipProbability":0.02,"precipType":"rain","temperature":71.45,"apparentTemperature":71.45,"dewPoint":58.45,"humidity":0.64,"pressure":1018.2,"windSpeed":4.49,"windGust":4.5,"windBearing":149,"cloudCover":0.8,"uvIndex":0,"visibility":10,"ozone":289.2},{"time":1596949200,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0006,"precipProbability":0.02,"precipType":"rain","temperature":72.17,"apparentTemperature":72.17,"dewPoint":58.65,"humidity":0.62,"pressure":1018.2,"windSpeed":4.67,"windGust":4.67,"windBearing":140,"cloudCover":0.74,"uvIndex":0,"visibility":10,"ozone":288},{"time":1596952800,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0008,"precipProbability":0.02,"precipType":"rain","temperature":74.33,"apparentTemperature":74.33,"dewPoint":59.52,"humidity":0.6,"pressure":1018.1,"windSpeed":5.03,"windGust":5.03,"windBearing":136,"cloudCover":0.64,"uvIndex":0,"visibility":10,"ozone":287.2},{"time":1596956400,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0006,"precipProbability":0.01,"precipType":"rain","temperature":78.43,"apparentTemperature":78.48,"dewPoint":60.09,"humidity":0.53,"pressure":1018,"windSpeed":5.75,"windGust":5.76,"windBearing":142,"cloudCover":0.47,"uvIndex":1,"visibility":10,"ozone":286.9},{"time":1596960000,"summary":"Clear","icon":"clear-day","precipIntensity":0.0003,"precipProbability":0.01,"precipType":"rain","temperature":82.96,"apparentTemperature":83.18,"dewPoint":60.06,"humidity":0.46,"pressure":1017.8,"windSpeed":6.66,"windGust":7.17,"windBearing":153,"cloudCover":0.25,"uvIndex":3,"visibility":10,"ozone":286.9},{"time":1596963600,"summary":"Clear","icon":"clear-day","precipIntensity":0.0003,"precipProbability":0.01,"precipType":"rain","temperature":86.39,"apparentTemperature":86.39,"dewPoint":59.65,"humidity":0.41,"pressure":1017.5,"windSpeed":7.37,"windGust":8.37,"windBearing":160,"cloudCover":0.09,"uvIndex":5,"visibility":10,"ozone":287},{"time":1596967200,"summary":"Clear","icon":"clear-day","precipIntensity":0.0002,"precipProbability":0.01,"precipType":"rain","temperature":88.3,"apparentTemperature":88.3,"dewPoint":58.81,"humidity":0.37,"pressure":1017.2,"windSpeed":7.81,"windGust":8.66,"windBearing":160,"cloudCover":0.05,"uvIndex":6,"visibility":10,"ozone":287.2},{"time":1596970800,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":89.33,"apparentTemperature":89.33,"dewPoint":57.79,"humidity":0.35,"pressure":1016.5,"windSpeed":8.06,"windGust":8.54,"windBearing":157,"cloudCover":0.05,"uvIndex":7,"visibility":10,"ozone":287.7},{"time":1596974400,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":90.65,"apparentTemperature":90.65,"dewPoint":56.97,"humidity":0.32,"pressure":1015.8,"windSpeed":8.06,"windGust":8.39,"windBearing":152,"cloudCover":0.05,"uvIndex":8,"visibility":10,"ozone":287.9},{"time":1596978000,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":91.97,"apparentTemperature":91.97,"dewPoint":56.28,"humidity":0.3,"pressure":1015.5,"windSpeed":7.72,"windGust":8.08,"windBearing":147,"cloudCover":0.04,"uvIndex":7,"visibility":10,"ozone":287.6},{"time":1596981600,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":92.73,"apparentTemperature":92.73,"dewPoint":55.49,"humidity":0.29,"pressure":1015.4,"windSpeed":7.17,"windGust":7.69,"windBearing":141,"cloudCover":0.03,"uvIndex":6,"visibility":10,"ozone":287},{"time":1596985200,"summary":"Clear","icon":"clear-day","precipIntensity":0.0002,"precipProbability":0.01,"precipType":"rain","temperature":92.17,"apparentTemperature":92.17,"dewPoint":55.02,"humidity":0.29,"pressure":1015.1,"windSpeed":6.56,"windGust":7.72,"windBearing":136,"cloudCover":0.02,"uvIndex":4,"visibility":10,"ozone":286.4},{"time":1596988800,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":91.11,"apparentTemperature":91.11,"dewPoint":55.35,"humidity":0.3,"pressure":1014.7,"windSpeed":5.92,"windGust":8.53,"windBearing":138,"cloudCover":0.01,"uvIndex":2,"visibility":10,"ozone":285.7},{"time":1596992400,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":89.49,"apparentTemperature":89.49,"dewPoint":57.01,"humidity":0.33,"pressure":1014.4,"windSpeed":5.2,"windGust":9.65,"windBearing":141,"cloudCover":0.01,"uvIndex":1,"visibility":10,"ozone":284.8},{"time":1596996000,"summary":"Clear","icon":"clear-day","precipIntensity":0,"precipProbability":0,"temperature":86.96,"apparentTemperature":86.96,"dewPoint":58.4,"humidity":0.38,"pressure":1014.2,"windSpeed":4.66,"windGust":10.14,"windBearing":138,"cloudCover":0.01,"uvIndex":0,"visibility":10,"ozone":284.5},{"time":1596999600,"summary":"Clear","icon":"clear-day","precipIntensity":0.0007,"precipProbability":0.04,"precipType":"rain","temperature":83.43,"apparentTemperature":83.68,"dewPoint":60.47,"humidity":0.46,"pressure":1014.6,"windSpeed":4.42,"windGust":9.33,"windBearing":125,"cloudCover":0.12,"uvIndex":0,"visibility":10,"ozone":284.8},{"time":1597003200,"summary":"Clear","icon":"clear-night","precipIntensity":0.0026,"precipProbability":0.07,"precipType":"rain","temperature":79.43,"apparentTemperature":79.69,"dewPoint":62.23,"humidity":0.56,"pressure":1015.2,"windSpeed":4.35,"windGust":7.87,"windBearing":105,"cloudCover":0.28,"uvIndex":0,"visibility":10,"ozone":285.5},{"time":1597006800,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0041,"precipProbability":0.08,"precipType":"rain","temperature":76.19,"apparentTemperature":76.52,"dewPoint":63.09,"humidity":0.64,"pressure":1015.6,"windSpeed":4.36,"windGust":6.8,"windBearing":92,"cloudCover":0.42,"uvIndex":0,"visibility":10,"ozone":286.4},{"time":1597010400,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0029,"precipProbability":0.08,"precipType":"rain","temperature":74.21,"apparentTemperature":74.57,"dewPoint":63.3,"humidity":0.69,"pressure":1015.6,"windSpeed":4.48,"windGust":6.65,"windBearing":96,"cloudCover":0.51,"uvIndex":0,"visibility":10,"ozone":287.4},{"time":1597014000,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0012,"precipProbability":0.06,"precipType":"rain","temperature":72.88,"apparentTemperature":73.24,"dewPoint":63.19,"humidity":0.72,"pressure":1015.3,"windSpeed":4.67,"windGust":6.86,"windBearing":106,"cloudCover":0.59,"uvIndex":0,"visibility":10,"ozone":288.5},{"time":1597017600,"summary":"Mostly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0006,"precipProbability":0.05,"precipType":"rain","temperature":71.78,"apparentTemperature":72.1,"dewPoint":62.69,"humidity":0.73,"pressure":1015.1,"windSpeed":4.74,"windGust":6.83,"windBearing":112,"cloudCover":0.63,"uvIndex":0,"visibility":10,"ozone":289.2},{"time":1597021200,"summary":"Mostly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0004,"precipProbability":0.03,"precipType":"rain","temperature":70.68,"apparentTemperature":70.9,"dewPoint":61.77,"humidity":0.73,"pressure":1014.8,"windSpeed":4.52,"windGust":6.08,"windBearing":117,"cloudCover":0.63,"uvIndex":0,"visibility":10,"ozone":288.9},{"time":1597024800,"summary":"Mostly Cloudy","icon":"partly-cloudy-night","precipIntensity":0,"precipProbability":0,"temperature":69.99,"apparentTemperature":70.12,"dewPoint":60.98,"humidity":0.73,"pressure":1014.6,"windSpeed":4.18,"windGust":5.08,"windBearing":121,"cloudCover":0.6,"uvIndex":0,"visibility":10,"ozone":288.1},{"time":1597028400,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0,"precipProbability":0,"temperature":69.92,"apparentTemperature":70.04,"dewPoint":60.8,"humidity":0.73,"pressure":1014.5,"windSpeed":4.02,"windGust":4.67,"windBearing":125,"cloudCover":0.57,"uvIndex":0,"visibility":10,"ozone":287.4},{"time":1597032000,"summary":"Partly Cloudy","icon":"partly-cloudy-night","precipIntensity":0.0003,"precipProbability":0.03,"precipType":"rain","temperature":70.36,"apparentTemperature":70.53,"dewPoint":61.34,"humidity":0.73,"pressure":1014.5,"windSpeed":4.12,"windGust":5.21,"windBearing":124,"cloudCover":0.59,"uvIndex":0,"visibility":10,"ozone":286.9},{"time":1597035600,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0008,"precipProbability":0.03,"precipType":"rain","temperature":70.97,"apparentTemperature":71.27,"dewPoint":62.48,"humidity":0.75,"pressure":1014.6,"windSpeed":4.39,"windGust":6.34,"windBearing":120,"cloudCover":0.61,"uvIndex":0,"visibility":10,"ozone":286.7},{"time":1597039200,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0012,"precipProbability":0.04,"precipType":"rain","temperature":72.73,"apparentTemperature":73.14,"dewPoint":63.61,"humidity":0.73,"pressure":1014.6,"windSpeed":4.77,"windGust":7.74,"windBearing":121,"cloudCover":0.63,"uvIndex":0,"visibility":10,"ozone":286.7},{"time":1597042800,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0008,"precipProbability":0.03,"precipType":"rain","temperature":75.8,"apparentTemperature":76.27,"dewPoint":64.42,"humidity":0.68,"pressure":1014.6,"windSpeed":5.33,"windGust":9.7,"windBearing":126,"cloudCover":0.62,"uvIndex":1,"visibility":10,"ozone":286.9},{"time":1597046400,"summary":"Mostly Cloudy","icon":"partly-cloudy-day","precipIntensity":0.0003,"precipProbability":0.02,"precipType":"rain","temperature":79.14,"apparentTemperature":79.66,"dewPoint":64.89,"humidity":0.62,"pressure":1014.6,"windSpeed":5.89,"windGust":11.93,"windBearing":135,"cloudCover":0.6,"uvIndex":3,"visibility":10,"ozone":287.5},{"time":1597050000,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0,"precipProbability":0,"temperature":81.84,"apparentTemperature":83.53,"dewPoint":64.65,"humidity":0.56,"pressure":1014.7,"windSpeed":6.23,"windGust":13.28,"windBearing":143,"cloudCover":0.58,"uvIndex":4,"visibility":10,"ozone":288},{"time":1597053600,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0,"precipProbability":0,"temperature":83.88,"apparentTemperature":84.92,"dewPoint":63.1,"humidity":0.5,"pressure":1014.5,"windSpeed":6.35,"windGust":13.1,"windBearing":146,"cloudCover":0.56,"uvIndex":5,"visibility":10,"ozone":288.2},{"time":1597057200,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0,"precipProbability":0,"temperature":85.71,"apparentTemperature":85.9,"dewPoint":61.12,"humidity":0.44,"pressure":1013.9,"windSpeed":6.31,"windGust":12.04,"windBearing":148,"cloudCover":0.53,"uvIndex":6,"visibility":10,"ozone":288.3},{"time":1597060800,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0,"precipProbability":0,"temperature":87.79,"apparentTemperature":87.79,"dewPoint":59.78,"humidity":0.39,"pressure":1013.6,"windSpeed":6.2,"windGust":11,"windBearing":145,"cloudCover":0.51,"uvIndex":6,"visibility":10,"ozone":288.4},{"time":1597064400,"summary":"Partly Cloudy","icon":"partly-cloudy-day","precipIntensity":0,"precipProbability":0,"temperature":89.65,"apparentTemperature":89.65,"dewPoint":59.04,"humidity":0.36,"pressure":1013.4,"windSpeed":6.07,"windGust":10.02,"windBearing":134,"cloudCover":0.47,"uvIndex":6,"visibility":10,"ozone":288.6}]},"daily":{"summary":"Rain on Wednesday through next Saturday.","icon":"rain","data":[{"time":1596837600,"summary":"Clear throughout the day.","icon":"clear-day","sunriseTime":1596860400,"sunsetTime":1596913860,"moonPhase":0.66,"precipIntensity":0.0003,"precipIntensityMax":0.0011,"precipIntensityMaxTime":1596909480,"precipProbability":0.03,"precipType":"rain","temperatureHigh":92.75,"temperatureHighTime":1596895500,"temperatureLow":70.39,"temperatureLowTime":1596942000,"apparentTemperatureHigh":92.26,"apparentTemperatureHighTime":1596895320,"apparentTemperatureLow":70.88,"apparentTemperatureLowTime":1596942000,"dewPoint":61.64,"humidity":0.57,"pressure":1019.3,"windSpeed":3.56,"windGust":5.65,"windGustTime":1596924000,"windBearing":103,"cloudCover":0,"uvIndex":8,"uvIndexTime":1596887160,"visibility":10,"ozone":294.1,"temperatureMin":65.77,"temperatureMinTime":1596853320,"temperatureMax":92.75,"temperatureMaxTime":1596895500,"apparentTemperatureMin":66.26,"apparentTemperatureMinTime":1596853320,"apparentTemperatureMax":92.26,"apparentTemperatureMaxTime":1596895320},{"time":1596924000,"summary":"Clear throughout the day.","icon":"clear-day","sunriseTime":1596946860,"sunsetTime":1597000200,"moonPhase":0.69,"precipIntensity":0.0006,"precipIntensityMax":0.0041,"precipIntensityMaxTime":1597006920,"precipProbability":0.15,"precipType":"rain","temperatureHigh":93.23,"temperatureHighTime":1596981780,"temperatureLow":69.39,"temperatureLowTime":1597026900,"apparentTemperatureHigh":92.73,"apparentTemperatureHighTime":1596981780,"apparentTemperatureLow":70,"apparentTemperatureLowTime":1597027020,"dewPoint":59.12,"humidity":0.49,"pressure":1016.7,"windSpeed":5.6,"windGust":10.15,"windGustTime":1596995700,"windBearing":140,"cloudCover":0.24,"uvIndex":8,"uvIndexTime":1596973440,"visibility":10,"ozone":287.6,"temperatureMin":70.39,"temperatureMinTime":1596942000,"temperatureMax":93.23,"temperatureMaxTime":1596981780,"apparentTemperatureMin":70.88,"apparentTemperatureMinTime":1596942000,"apparentTemperatureMax":92.73,"apparentTemperatureMaxTime":1596981780},{"time":1597010400,"summary":"Partly cloudy throughout the day.","icon":"partly-cloudy-day","sunriseTime":1597033380,"sunsetTime":1597086480,"moonPhase":0.72,"precipIntensity":0.0007,"precipIntensityMax":0.0029,"precipIntensityMaxTime":1597010400,"precipProbability":0.2,"precipType":"rain","temperatureHigh":91.35,"temperatureHighTime":1597068720,"temperatureLow":68.83,"temperatureLowTime":1597115160,"apparentTemperatureHigh":90.85,"apparentTemperatureHighTime":1597068720,"apparentTemperatureLow":69.54,"apparentTemperatureLowTime":1597115100,"dewPoint":62.06,"humidity":0.58,"pressure":1014.1,"windSpeed":5.34,"windGust":13.9,"windGustTime":1597083480,"windBearing":119,"cloudCover":0.45,"uvIndex":6,"uvIndexTime":1597060440,"visibility":10,"ozone":288,"temperatureMin":69.39,"temperatureMinTime":1597026900,"temperatureMax":91.35,"temperatureMaxTime":1597068720,"apparentTemperatureMin":70,"apparentTemperatureMinTime":1597027020,"apparentTemperatureMax":90.85,"apparentTemperatureMaxTime":1597068720},{"time":1597096800,"summary":"Mostly cloudy throughout the day.","icon":"partly-cloudy-day","sunriseTime":1597119840,"sunsetTime":1597172760,"moonPhase":0.75,"precipIntensity":0.0002,"precipIntensityMax":0.001,"precipIntensityMaxTime":1597168920,"precipProbability":0.05,"precipType":"rain","temperatureHigh":91.89,"temperatureHighTime":1597154640,"temperatureLow":72.54,"temperatureLowTime":1597198140,"apparentTemperatureHigh":91.39,"apparentTemperatureHighTime":1597154640,"apparentTemperatureLow":73.28,"apparentTemperatureLowTime":1597198020,"dewPoint":61.24,"humidity":0.55,"pressure":1013.9,"windSpeed":5.87,"windGust":18.06,"windGustTime":1597183200,"windBearing":126,"cloudCover":0.63,"uvIndex":6,"uvIndexTime":1597147080,"visibility":10,"ozone":286.1,"temperatureMin":68.83,"temperatureMinTime":1597115160,"temperatureMax":91.89,"temperatureMaxTime":1597154640,"apparentTemperatureMin":69.54,"apparentTemperatureMinTime":1597115100,"apparentTemperatureMax":91.39,"apparentTemperatureMaxTime":1597154640},{"time":1597183200,"summary":"Rain in the evening and overnight.","icon":"rain","sunriseTime":1597206360,"sunsetTime":1597259040,"moonPhase":0.78,"precipIntensity":0.0142,"precipIntensityMax":0.0804,"precipIntensityMaxTime":1597256760,"precipProbability":0.64,"precipType":"rain","temperatureHigh":91.71,"temperatureHighTime":1597240980,"temperatureLow":63.88,"temperatureLowTime":1597294920,"apparentTemperatureHigh":91.21,"apparentTemperatureHighTime":1597240980,"apparentTemperatureLow":65.18,"apparentTemperatureLowTime":1597294920,"dewPoint":63.04,"humidity":0.59,"pressure":1012.2,"windSpeed":8.78,"windGust":27.15,"windGustTime":1597254780,"windBearing":144,"cloudCover":0.13,"uvIndex":8,"uvIndexTime":1597232580,"visibility":9.404,"ozone":284.5,"temperatureMin":68.24,"temperatureMinTime":1597269600,"temperatureMax":91.71,"temperatureMaxTime":1597240980,"apparentTemperatureMin":69.47,"apparentTemperatureMinTime":1597269600,"apparentTemperatureMax":91.21,"apparentTemperatureMaxTime":1597240980},{"time":1597269600,"summary":"Possible light rain in the morning.","icon":"rain","sunriseTime":1597292820,"sunsetTime":1597345320,"moonPhase":0.81,"precipIntensity":0.0037,"precipIntensityMax":0.0323,"precipIntensityMaxTime":1597269600,"precipProbability":0.73,"precipType":"rain","temperatureHigh":78.34,"temperatureHighTime":1597330020,"temperatureLow":59.84,"temperatureLowTime":1597377060,"apparentTemperatureHigh":77.87,"apparentTemperatureHighTime":1597329840,"apparentTemperatureLow":60.32,"apparentTemperatureLowTime":1597377000,"dewPoint":62.63,"humidity":0.79,"pressure":1014.2,"windSpeed":6.6,"windGust":17.05,"windGustTime":1597292340,"windBearing":230,"cloudCover":0.86,"uvIndex":5,"uvIndexTime":1597319820,"visibility":9.755,"ozone":297.3,"temperatureMin":63.88,"temperatureMinTime":1597294920,"temperatureMax":78.34,"temperatureMaxTime":1597330020,"apparentTemperatureMin":65,"apparentTemperatureMinTime":1597356000,"apparentTemperatureMax":77.87,"apparentTemperatureMaxTime":1597329840},{"time":1597356000,"summary":"Rain in the evening and overnight.","icon":"rain","sunriseTime":1597379340,"sunsetTime":1597431660,"moonPhase":0.85,"precipIntensity":0.0132,"precipIntensityMax":0.0847,"precipIntensityMaxTime":1597438020,"precipProbability":0.7,"precipType":"rain","temperatureHigh":79.02,"temperatureHighTime":1597413660,"temperatureLow":55.07,"temperatureLowTime":1597467840,"apparentTemperatureHigh":78.52,"apparentTemperatureHighTime":1597413660,"apparentTemperatureLow":55.56,"apparentTemperatureLowTime":1597467840,"dewPoint":56.34,"humidity":0.68,"pressure":1012.7,"windSpeed":7.3,"windGust":18.16,"windGustTime":1597429200,"windBearing":204,"cloudCover":0.68,"uvIndex":7,"uvIndexTime":1597405020,"visibility":8.67,"ozone":301.6,"temperatureMin":59.84,"temperatureMinTime":1597377060,"temperatureMax":79.02,"temperatureMaxTime":1597413660,"apparentTemperatureMin":60.32,"apparentTemperatureMinTime":1597377000,"apparentTemperatureMax":78.52,"apparentTemperatureMaxTime":1597413660},{"time":1597442400,"summary":"Mostly cloudy throughout the day.","icon":"rain","sunriseTime":1597465800,"sunsetTime":1597517940,"moonPhase":0.88,"precipIntensity":0.0052,"precipIntensityMax":0.0623,"precipIntensityMaxTime":1597442400,"precipProbability":0.76,"precipType":"rain","temperatureHigh":71.77,"temperatureHighTime":1597503660,"temperatureLow":53.15,"temperatureLowTime":1597543260,"apparentTemperatureHigh":71.27,"apparentTemperatureHighTime":1597503660,"apparentTemperatureLow":53.64,"apparentTemperatureLowTime":1597543260,"dewPoint":52.42,"humidity":0.73,"pressure":1014.1,"windSpeed":7,"windGust":16.33,"windGustTime":1597499520,"windBearing":250,"cloudCover":0.71,"uvIndex":5,"uvIndexTime":1597492680,"visibility":10,"ozone":313.3,"temperatureMin":55.07,"temperatureMinTime":1597467840,"temperatureMax":71.77,"temperatureMaxTime":1597503660,"apparentTemperatureMin":55.56,"apparentTemperatureMinTime":1597467840,"apparentTemperatureMax":71.27,"apparentTemperatureMaxTime":1597503660}]},"flags":{"sources":["meteoalarm","cmc","gfs","icon","isd","madis"],"meteoalarm-license":"Based on data from EUMETNET - MeteoAlarm [https://www.meteoalarm.eu/]. Time delays between this website and the MeteoAlarm website are possible; for the most up to date information about alert levels as published by the participating National Meteorological Services please use the MeteoAlarm website.","nearest-station":5.274,"units":"us"},"offset":2}
"""
