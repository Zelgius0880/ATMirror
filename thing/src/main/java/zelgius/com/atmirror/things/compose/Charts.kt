package zelgius.com.atmirror.things.compose

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import zelgius.com.atmirror.things.compose.custom.Chart
import zelgius.com.atmirror.things.compose.custom.ChartXAxis
import zelgius.com.atmirror.things.compose.custom.ChartYAxis
import zelgius.com.atmirror.things.entities.SensorRecord
import zelgius.com.contextextensions.dpToPx
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PressureChart(history: List<SensorRecord>) {
    val now = Date().time
    val data = LineData(
        listOf(
            LineDataSet(
                history.map {
                    val hour = (it.stamp - now) / (60 * 60 * 1000)
                    Entry(hour.toFloat(), it.pressure.toFloat())
                },
                null
            ).apply {
                setDrawCircles(false)
                setDrawFilled(true)
                setDrawValues(false)
                lineWidth = 2f
                label = null
                color = Color.WHITE
                fillColor = Color.WHITE
                fillAlpha = 64
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            }
        )
    )

    /*   val max = (list.maxByOrNull { it.pressure }?.pressure?.toFloat()?:0f)  + 3
       val min = (list.minByOrNull { it.pressure }?.pressure?.toFloat()?: 0f) - 3
       val granularity = (max - min) / 4*/

    DrawChart(
        data = data,
        /*min = min,
        max = max,
        granularity = granularity,*/
        formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return String.format("%.0fh", value)
            }
        })
}

@Composable
private fun DrawChart(
    data: LineData,
    max: Float? = null,
    min: Float? = null,
    granularity: Float? = null,
    formatter: ValueFormatter? = null
) {
    val context = LocalContext.current
    val chart = remember {
        Chart(
            context = context,
            data = data,
            size = context.dpToPx(300f).roundToInt() to context.dpToPx(120f).roundToInt(),
            rightAxis = ChartYAxis(isEnabled = false),
            backgroundColor = Color.TRANSPARENT,
            extraBottomOffset = 4f,
            leftAxis = ChartYAxis(
                min = min,
                max = max,
                axisColor = Color.WHITE,
                textColor = Color.WHITE,
                textSize = 14f,
                width = 2f,
                granularity = granularity,
                drawGridLines = false
            ),
            x = ChartXAxis(
                position = XAxis.XAxisPosition.BOTTOM,
                textColor = Color.WHITE,
                axisColor = Color.WHITE,
                textSize = 14f,
                max = 0f,
                min = -24f,
                drawGridLines = false,
                width = 2f,
                formatter = formatter
            )
        )
    }


    // Adds view to Compose
    AndroidView({ chart }) {
        // View's been inflated - add logic here if necessary

        // As selectedItem is read here, AndroidView will recompose
        // whenever the state changes
        // Example of Compose -> View communication
        it.data = data
        it.invalidate()
    }

    chart.apply {
        this.data = data
        leftAxis?.applyOnAxis(axisLeft)
        rightAxis?.applyOnAxis(axisRight)
        x?.applyOnAxis(this.xAxis)
        invalidate()
    }
}

@Composable
fun TemperatureChart(history: List<SensorRecord>) {
    val now = Date().time
    val data = LineData(
        listOf(
            LineDataSet(
                history.map {
                    val hour = (it.stamp - now) / (60 * 60 * 1000)
                    Entry(hour.toFloat(), it.temperature.toFloat())
                },
                "Temperature"
            ).apply {
                setDrawCircles(false)
                setDrawValues(false)
                setDrawFilled(true)
                lineWidth = 2f
                label = null
                color = Color.RED
                fillColor = Color.RED
                fillAlpha = 32
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            }
        )
    )

    val max = (history.minByOrNull { it.temperature }?.temperature?.toFloat() ?: 0f) + 3
    val min = (history.maxByOrNull { it.temperature }?.temperature?.toFloat() ?: 0f) - 3
    //val granularity = (max - min) / 2

    DrawChart(data = data,
        /*min = min,
        max = max,*/
        //granularity = 2f,
        formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return String.format("%.0fh", value)
            }
        })
}


@Composable
@Preview
fun PreviewPressure() {
    val now = Date().time
    val list = (0 until 24).map {
        SensorRecord().apply {
            temperature = Random.nextDouble(16.0, 27.0)
            pressure = Random.nextDouble(966.0, 1024.0)
            stamp = now - it * 60 * 60 * 1000
        }
    }

    PressureChart(history = list)
}

@Composable
@Preview
fun PreviewChart() {
    val now = Date().time
    val list = (0 until 24).map {
        SensorRecord().apply {
            temperature = Random.nextDouble(16.0, 27.0)
            pressure = Random.nextDouble(966.0, 1024.0)
            stamp = now - it * 60 * 60 * 1000
        }
    }

    Column(modifier = Modifier.size(300.dp, 400.dp)) {
        PressureChart(
            history = list
        )
        TemperatureChart(history = list)
    }
}