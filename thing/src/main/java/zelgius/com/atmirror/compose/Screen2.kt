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
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
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
import zelgius.com.atmirror.entities.json.ForecastData
import zelgius.com.atmirror.entities.json.OpenWeatherMap
import zelgius.com.utils.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.compose.ui.graphics.Color as CColor

data class Screen2(val forecast: OpenWeatherMap) {
    var bitmap: Bitmap? = null


    constructor(forecast: OpenWeatherMap, bitmap: Bitmap) : this(forecast) {
        this.bitmap = bitmap
    }
}


@Composable
fun Screen2View(forecast: MutableState<OpenWeatherMap>) {
    Box(Modifier.size(300.dp, 400.dp)) {
        forecast.value.apply {
            if (list.size >= 6) {
                (0 until 6 step 2).forEach {
                    Row {
                        ForecastCell(item = list[it])
                        ForecastCell(item = list[it + 1])
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastCell(item: ForecastData) {

    Column(modifier = Modifier.size(150.dp, 133.dp) + Modifier.padding(top = 4.dp)) {

        ForecastCellHeader(item)

        Divider(
            color = CColor.Red,
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 2.dp
        )

        Stack(modifier = Modifier.height(90.dp) + Modifier.fillMaxWidth()) {
            Image(
                vectorResource(id = getIconId(item)),
                modifier = Modifier.fillMaxHeight() + Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillHeight,
                colorFilter = tint(CColor.White)
            )

            Box(
                gravity = ContentGravity.TopStart,
                modifier = Modifier.fillMaxHeight() + Modifier.fillMaxWidth()
                        + Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    String.format("%.1f", item.temp.max), style = TextStyle(
                        fontSize = 24.sp, color = CColor.White,
                        shadow = Shadow(
                            color = CColor.Black,
                            offset = Offset(0f, 0f),
                            blurRadius = 1f
                        )

                    )
                )
            }

            Box(
                gravity = ContentGravity.BottomEnd,
                modifier = Modifier.fillMaxHeight() + Modifier.fillMaxWidth()
                        + Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    String.format("%.1f", item.temp.min),
                    style = TextStyle(
                        fontSize = 24.sp, color = CColor.White,
                        shadow = Shadow(
                            color = CColor.Black,
                            offset = Offset(0f, 0f),
                            blurRadius = 1f
                        )

                    )
                )
            }
        }
    }
}

fun getIconId(item: ForecastData) =
    when (item.weather.first().id) { // the first one is the main
        in (200 .. 232) -> R.drawable.ic_wi_thunderstorm
        in (300 .. 321) -> R.drawable.ic_wi_rain
        in (500 .. 521) -> R.drawable.ic_wi_rain
        611,612,613 -> R.drawable.ic_wi_sleet
        in (600 .. 622) -> R.drawable.ic_wi_snow
        800 -> R.drawable.ic_wi_day_sunny
        801 -> R.drawable.ic_wi_day_cloudy
        in (802 .. 804) -> R.drawable.ic_wi_cloud
        701, 711, 721, 731, 741, 751, 761, 762 -> R.drawable.ic_wi_fog
        771 -> R.drawable.ic_wi_strong_wind
        781 -> R.drawable.ic_wi_tornado


        //"hail" -> R.drawable.ic_wi_hail
        else -> R.drawable.ic_wi_na
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
                    modifier = Modifier.width(14.dp),
                    asset = vectorResource(id = R.drawable.ic_wi_rain),
                    colorFilter = tint(CColor.White),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter
                )

                Box(modifier = Modifier.height(30.dp), gravity = ContentGravity.CenterEnd) {
                    Text(
                        text = String.format("%.0f%%", item.pop * 100),
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
    ForecastCell(Gson().fromJson(sample, OpenWeatherMap::class.java).list.first())
}

@Composable
@Preview
private fun Preview() {

    //Box(Modifier.size(300.dp, 400.dp)) {
        Screen2View(state { Gson().fromJson(sample, OpenWeatherMap::class.java) })
    //}
}

const val sample = """
    {"city":{"id":2790451,"name":"Nassogne","coord":{"lon":5.3427,"lat":50.1285},"country":"BE","population":5081,"timezone":7200},"cod":"200","message":0.0901728,"cnt":6,"list":[{"dt":1597316400,"sunrise":1597292746,"sunset":1597345289,"temp":{"day":28.89,"min":16.72,"max":28.89,"night":16.72,"eve":23.81,"morn":28.89},"feels_like":{"day":28.19,"night":16.56,"eve":24.69,"morn":28.19},"pressure":1010,"humidity":44,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":3.52,"deg":214,"clouds":70,"pop":0.4,"rain":1.03},{"dt":1597402800,"sunrise":1597379236,"sunset":1597431578,"temp":{"day":20.1,"min":15.79,"max":22.03,"night":15.79,"eve":18.91,"morn":17.32},"feels_like":{"day":19.19,"night":16.1,"eve":19.61,"morn":17.15},"pressure":1013,"humidity":76,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":4,"deg":201,"clouds":75,"pop":0.81,"rain":2.31},{"dt":1597489200,"sunrise":1597465726,"sunset":1597517866,"temp":{"day":23,"min":15.36,"max":23,"night":15.36,"eve":20.58,"morn":15.93},"feels_like":{"day":23.18,"night":16.4,"eve":22.17,"morn":16.34},"pressure":1014,"humidity":65,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":2.62,"deg":262,"clouds":61,"pop":0.4,"rain":0.73},{"dt":1597575600,"sunrise":1597552216,"sunset":1597604153,"temp":{"day":26.5,"min":15.99,"max":26.58,"night":15.99,"eve":22.25,"morn":16.89},"feels_like":{"day":26.6,"night":15.99,"eve":23.4,"morn":17.85},"pressure":1010,"humidity":45,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":1.47,"deg":183,"clouds":26,"pop":1,"rain":4.13},{"dt":1597662000,"sunrise":1597638706,"sunset":1597690439,"temp":{"day":21.34,"min":13.26,"max":21.34,"night":13.26,"eve":17.92,"morn":16.49},"feels_like":{"day":20.12,"night":12.92,"eve":18.48,"morn":18.01},"pressure":1011,"humidity":66,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":3.91,"deg":233,"clouds":100,"pop":1,"rain":7.66},{"dt":1597748400,"sunrise":1597725196,"sunset":1597776723,"temp":{"day":16.63,"min":12.6,"max":17.31,"night":12.6,"eve":15.59,"morn":13.07},"feels_like":{"day":14.19,"night":12.08,"eve":14.94,"morn":12.48},"pressure":1013,"humidity":80,"weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],"speed":4.9,"deg":245,"clouds":73,"pop":0.99,"rain":6.61}]}
"""
