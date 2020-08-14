package zelgius.com.atmirror.compose


import android.graphics.*
import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.State
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.ColorFilter.Companion.tint
import androidx.ui.layout.*
import androidx.ui.res.vectorResource
import androidx.ui.text.TextStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import zelgius.com.atmirror.R
import zelgius.com.atmirror.entities.SensorRecord
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random
import androidx.ui.graphics.Color as CColor

data class Screen1(
    val pressure: Int?,
    val humidity: Int?,
    val temperature: Float?,
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
    pressure: State<Int?>,
    humidity: MutableState<Int?>,
    history: State<List<SensorRecord>>
) {
    Column(modifier = Modifier.size(300.dp, 400.dp)) {

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
            modifier = Modifier.padding(start = 16.dp, top = 18.dp)
        )

        Box(Modifier.fillMaxWidth() + Modifier.height(120.dp)) {
            TemperatureChart(history = history)
        }

        Pressure(pressure = pressure, humidity = humidity)

        Box(Modifier.fillMaxWidth() + Modifier.height(120.dp)) {

            PressureChart(history = history)
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
        pressure = statePressure,
        humidity = stateHumidity,
        history = stateHistory
    )
}