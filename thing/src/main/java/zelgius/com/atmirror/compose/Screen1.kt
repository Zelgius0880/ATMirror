package zelgius.com.atmirror.compose

import android.graphics.*
import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color as CColor

import androidx.ui.tooling.preview.Preview

import zelgius.com.atmirror.R
import zelgius.com.atmirror.entities.SensorRecord
import kotlin.math.ceil

data class Screen1(val pressure: Float?, val temperature: Float?) {
    var bitmap: Bitmap? = null

    constructor(pressure: Float?, temperature: Float?, bitmap: Bitmap):  this(pressure, temperature){
        this.bitmap = bitmap
    }
}

@Composable
fun Screen1View(
    temperature: State<Float?>,
    pressure: MutableState<Int?>,
    history: State<List<SensorRecord>>
) {
    Column(modifier = Modifier.size(300.dp, 400.dp)) {
        Box(
            gravity = ContentGravity.TopCenter,
            modifier = Modifier.fillMaxWidth(),
            paddingTop = 18.dp
        ) {
            Temperature(temperature = temperature)
        }

        /*Box(
            gravity = ContentGravity.TopCenter,
            modifier = Modifier.height(120.dp) + Modifier.fillMaxWidth(),
            paddingTop = 18.dp
        ) {
            TemperatureHistory(history = history)
        }*/

        Box(
            modifier = Modifier.fillMaxWidth(),
            paddingTop = 18.dp
        ) {
            Pressure(pressure = pressure)
        }
    }
}

@Composable
fun Temperature(temperature: State<Float?>) {
    Text(
        "${
            if (temperature.value != null)
                String.format("%.1f", temperature.value)
            else "---"
        } °C",
        style = TextStyle(fontSize = 36.sp, color = CColor(Color.WHITE))
    )
}

/*
@Composable
fun TemperatureHistory(history: State<List<SensorRecord>>) {
    */
/* val series = listOf(x)
     val data = LineData(series.map {
         LineDataSet(it.mapIndexed { index, value ->
             Entry(index.toFloat(), value.toFloat())
         }, "DataSet 1").apply {
             setDrawCircles(false)
             lineWidth = 2.dp.value
         }
     })

     LineChart(ContextAmbient.current).apply {
         this.data = data
         layoutParams = ViewGroup.LayoutParams(
             TypedValue.applyDimension(
                 TypedValue.COMPLEX_UNIT_DIP,
                 300f,
                 ContextAmbient.current.resources.displayMetrics
             ).toInt(),
             TypedValue.applyDimension(
                 TypedValue.COMPLEX_UNIT_DIP,
                 120f,
                 ContextAmbient.current.resources.displayMetrics
             ).toInt()
         )
     }
 *//*

}
*/


const val BAROMETER_RANGE_LOW = 965f
const val BAROMETER_RANGE_HIGH = 1035f

@Composable
fun Pressure(pressure: State<Int?>) {
    Row {
        Box(
            gravity = Alignment.TopStart,
            paddingStart = 16.dp
        ) {
            Text(
                "${
                    if (pressure.value != null)
                        String.format("%d", pressure.value!!)
                    else "---"
                } Pa",
                style = TextStyle(
                    fontSize = 36.sp,
                    color = CColor(Color.WHITE)
                ),
            )
        }

        ForecastByPressure(pressure = pressure)
    }
}


@Composable
fun ForecastByPressure(pressure: State<Int?>) {
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

        Box(modifier = Modifier.fillMaxWidth(), gravity = Alignment.CenterEnd, paddingEnd = 16.dp) {
            Image(
                asset = vectorResource(id = barometerIcons[n]),
                modifier = Modifier.size(56.dp, 56.dp),
                colorFilter = tint(CColor.White)
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    val stateTemperature: MutableState<Float?> = state { null }
    val statePressure: MutableState<Int?> = state { null }
    val stateHistory: MutableState<List<SensorRecord>> = state { listOf() }

    /*Row {

        Screen1(
            history = stateHistory,
            temperature = stateTemperature,
            pressure = statePressure
        )
    }*/

    Temperature(temperature = stateTemperature)
    stateTemperature.value = 21f
    statePressure.value = 1024
}