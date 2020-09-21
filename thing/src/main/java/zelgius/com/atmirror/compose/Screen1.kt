package zelgius.com.atmirror.compose

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.foundation.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.ui.tooling.preview.Preview
import zelgius.com.atmirror.R
import zelgius.com.atmirror.entities.SensorRecord
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
    temperature: State<Float?>,
    temperatureExternal: State<Float?>,
    pressure: State<Int?>,
    humidity: MutableState<Int?>,
    history: State<List<SensorRecord>>
) {
    Column(modifier = Modifier.size(300.dp, 400.dp)) {

        Temperature(temperature, temperatureExternal)

        Box(Modifier.fillMaxWidth() + Modifier.height(120.dp)) {
            TemperatureChart(history = history)
        }

        Pressure(pressure = pressure, humidity = humidity)

        Box(Modifier.fillMaxWidth() + Modifier.height(120.dp)) {

            PressureChart(history = history)
        }
    }
}

@Composable
private fun Temperature(temperature: State<Float?>, temperatureExternal: State<Float?>, ) {
    Row() {

        Column(modifier = Modifier.weight(1f, true)) {
            Text(
                stringResource(id = R.string.temperature_in).toUpperCase(Locale.getDefault()),
                style = TextStyle(
                    fontSize = 16.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.RED),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
            )
            Text(
                "${
                    if (temperature.value != null)
                        String.format("%.1f", temperature.value)
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
                stringResource(id = R.string.temperature_out).toUpperCase(Locale.getDefault()),
                style = TextStyle(
                    fontSize = 16.sp, textAlign = TextAlign.Start,
                    color = CColor(Color.RED),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp)
            )
            Text(
                "${
                    if (temperatureExternal.value != null)
                        String.format("%.1f", temperatureExternal.value)
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
fun Pressure(pressure: State<Int?>, humidity: MutableState<Int?>) {
    Row(
        modifier = Modifier.fillMaxWidth() + Modifier.padding(top = 18.dp)
    ) {
        Text(
            "${
                if (pressure.value != null)
                    String.format("%d", pressure.value!!)
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
fun ForecastByPressure(pressure: State<Int?>, humidity: MutableState<Int?>) {
    val p = pressure.value
    if (p != null) {
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

        val t = (p - BAROMETER_RANGE_LOW) / (BAROMETER_RANGE_HIGH - BAROMETER_RANGE_LOW)
        var n = ceil(barometerIcons.size * t).toInt()
        n = 0.coerceAtLeast(n.coerceAtMost(barometerIcons.size - 1))

        Row(
            modifier = Modifier.fillMaxWidth() + Modifier.padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                asset = vectorResource(id = barometerIcons[n]),
                modifier = Modifier.size(56.dp, 56.dp) + Modifier.padding(4.dp),
                contentScale = ContentScale.FillWidth,
                colorFilter = tint(CColor.White)
            )

            Humidity(humidity)
        }
    }
}

@Composable
private fun Humidity(humidity: MutableState<Int?>) {
    Row {

        Image(
            modifier = Modifier.width(12.dp),
            asset = vectorResource(id = R.drawable.ic_wi_raindrop),
            colorFilter = tint(CColor.White),
            contentScale = ContentScale.Crop
        )

        Text(
            "${
                if (humidity.value != null)
                    String.format("%d", humidity.value!!)
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
    val stateTemperature: MutableState<Float?> = state { 21f }
    val stateTemperatureExternal: MutableState<Float?> = state { 19f }
    val statePressure: MutableState<Int?> = state { 988 }
    val stateHumidity: MutableState<Int?> = state { 50 }
    val now = Date().time
    val stateHistory: MutableState<List<SensorRecord>> = state {
        (0 until 24).map {
            SensorRecord().apply {
                temperature = Random.nextDouble(16.0, 27.0)
                pressure = Random.nextDouble(966.0, 1024.0)
                stamp = now - it * 60 * 60 * 1000
            }
        }
    }

    Screen1View(
        temperature = stateTemperature,
        temperatureExternal = stateTemperatureExternal,
        pressure = statePressure,
        humidity = stateHumidity,
        history = stateHistory
    )
}
