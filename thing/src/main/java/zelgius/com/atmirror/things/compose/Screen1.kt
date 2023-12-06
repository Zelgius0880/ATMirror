package zelgius.com.atmirror.things.compose

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import zelgius.com.atmirror.things.R
import zelgius.com.atmirror.things.entities.SensorRecord
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random
import androidx.compose.ui.graphics.Color as CColor

data class Screen1(
    val pressure: Int?,
    val humidity: Int?,
    val temperature: Float?,
    val temperatureExternal: Float?,
    val history: List<SensorRecord>
) {
    var bitmap: Bitmap? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Screen1

        if (pressure != other.pressure) return false
        if (temperature != other.temperature) return false
        //if (!history .containsAll( other.history)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pressure?.hashCode() ?: 0
        result = 31 * result + (temperature?.hashCode() ?: 0)
        result = 31 * result + history.hashCode()
        return result
    }
}

@Composable
fun Screen1View(
    temperature: Float?,
    temperatureExternal: Float?,
    pressure: Int?,
    humidity: Int?,
    history: List<SensorRecord>
) {
    Column(modifier = Modifier.size(300.dp, 400.dp)) {

        Temperature(temperature, temperatureExternal)

        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            TemperatureChart(history = history)
        }

        Pressure(pressure = pressure, humidity = humidity)

        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {

            PressureChart(history = history)
        }
    }
}

@Composable
private fun Temperature(temperature: Float?, temperatureExternal: Float?) {
    Row {

        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                stringResource(id = R.string.temperature_in).uppercase(Locale.getDefault()),
                style = TextStyle(
                    fontSize = 16.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.RED),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
            )
            Text(
                "${
                    if (temperature != null)
                        String.format("%.1f", temperature)
                    else "---"
                } °C",
                style = TextStyle(
                    fontSize = 36.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.WHITE)
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                stringResource(id = R.string.temperature_out).uppercase(Locale.getDefault()),
                style = TextStyle(
                    fontSize = 16.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.RED),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
            )
            Text(
                "${
                    if (temperatureExternal != null)
                        String.format("%.1f", temperatureExternal)
                    else "---"
                } °C",
                style = TextStyle(
                    fontSize = 36.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.WHITE)
                ),
                modifier = Modifier.padding(start = 16.dp)
            )
        }

    }

}

const val BAROMETER_RANGE_LOW = 965f
const val BAROMETER_RANGE_HIGH = 1035f

@Composable
fun Pressure(pressure: Int?, humidity: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Text(
            "${
                if (pressure != null)
                    String.format("%d", pressure )
                else "---"
            } Pa",
            style = TextStyle(
                fontSize = 36.sp,
                color = CColor(Color.WHITE),
                textAlign = TextAlign.Start
            ),
            modifier = Modifier.padding(start = 16.dp)
        )

        ForecastByPressure(pressure = pressure, humidity = humidity)
    }
}


@Composable
fun ForecastByPressure(pressure: Int?, humidity: Int?) {
    if (pressure != null) {
        val barometerIcons =
            arrayListOf(
                R.drawable.ic_storm,
                R.drawable.ic_rain,
                R.drawable.ic_rain,
                R.drawable.ic_cloud,
                R.drawable.ic_cloudy,
                R.drawable.ic_sun,
                R.drawable.ic_sun
            )

        val t = (pressure - BAROMETER_RANGE_LOW) / (BAROMETER_RANGE_HIGH - BAROMETER_RANGE_LOW)
        var n = ceil(barometerIcons.size * t).toInt()
        n = 0.coerceAtLeast(n.coerceAtMost(barometerIcons.size - 1))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painterResource(id = barometerIcons[n]),
                modifier = Modifier
                    .size(56.dp, 56.dp)
                    .padding(4.dp),
                contentScale = ContentScale.FillWidth,
                colorFilter = tint(CColor.White),
                contentDescription = null
            )

            Humidity(humidity)
        }
    }
}

@Composable
private fun Humidity(humidity: Int?) {
    Row {

        Image(
            painterResource(id = R.drawable.ic_wi_raindrop),
            modifier = Modifier.width(12.dp),
            colorFilter = tint(CColor.White),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Text(
            "${
                if (humidity != null)
                    String.format("%d", humidity)
                else "---"
            }%",
            style = TextStyle(
                fontSize = 36.sp,
                color = CColor(Color.WHITE),
                textAlign = TextAlign.End
            )
        )
    }
}

@Composable
@Preview
private fun Preview() {
    val temperature = 21f
    val temperatureExternal = 19f
    val pressure = 988
    val humidity = 50
    val now = Date().time
    val history =
        (0 until 24).map {
            SensorRecord().apply {
                this.temperature = Random.nextDouble(16.0, 27.0)
                this.pressure = Random.nextDouble(966.0, 1024.0)
                stamp = now - it * 60 * 60 * 1000
            }
        }

    Screen1View(
        temperature = temperature,
        temperatureExternal = temperatureExternal,
        pressure = pressure,
        humidity = humidity,
        history = history
    )
}
