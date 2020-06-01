package zelgius.com.atmirror.shared


import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import khronos.Dates
import kotlinx.android.synthetic.main.fragment_perssure.*
import kotlinx.android.synthetic.main.fragment_perssure.graphView
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel
import zelgius.com.contextextensions.getColor
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SharedPressureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
abstract class SharedPressureFragment : Fragment() {


    private val context by lazy { activity!! }
    private val barometerIcons by lazy {
        arrayListOf(
            R.drawable.ic_storm,
            R.drawable.ic_rain,
            R.drawable.ic_rain,
            R.drawable.ic_cloud,
            R.drawable.ic_cloudy,
            R.drawable.ic_sun,
            R.drawable.ic_sun
        )
    }

    protected abstract val viewModel : SharedMainViewModel


    private var oldPressure = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perssure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.piclockCurrentRecord.observe(this, Observer {

            //Convert pressure to the equivalent at see level
            /* val p = (it.pressure * Math.pow(
                 1 - 0.0065 * it.altitude / (it.temperature + 0.0065 * it.altitude + 273.15),
                 -5.257
             )).toFloat()*/
            // Pressure from firebase piclock is already converted

            viewModel.updateLastKnownRecord(it)

            //if (it.pressure.roundToInt() != oldPressure.roundToInt()) {
            rh.text = String.format("%.0f Pa", it.pressure)

            oldPressure = it.pressure
            updateBarometer(it.pressure)
            //}
        })

        viewModel.history.observe(this, Observer {
            //textView.text = "${getString(R.string.temperature_title)} ${SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())}"
            if (it.isNotEmpty()) {
                //textView.text = "${getString(R.string.temperature_title)} ${it.size} ${SimpleDateFormat("HH:mm", Locale.FRANCE).format(it.last().date)}"

                //it.asReversed()
               /* val x = mutableListOf<Double>()
                val y = mutableListOf<Double>()
                it.forEach { d ->
                    x.add(d.stamp.toDouble())
                    y.add(d.pressure.round(0))
                }

                with(PolynomialRegression(DoubleArray(x.size) { i -> i.toDouble()}, y.toDoubleArray(), 3)) {
                    val predictedY = predict((Dates.today + 1.hour).time.toDouble()).roundToInt()
                    println(R2())
                    prediction.setImageResource(
                        when {
                            predictedY > viewModel.lastKnownRecord.pressure -> R.drawable.ic_curve_arrow_up
                            predictedY < viewModel.lastKnownRecord.pressure -> R.drawable.ic_curve_arrow_down
                            else -> R.drawable.ic_right_arrow
                        }
                    )
                }*/

                prediction.setImageResource(
                    when {
                        it.last().pressure < viewModel.lastKnownRecord.pressure -> R.drawable.ic_curve_arrow_up
                        it.last().pressure > viewModel.lastKnownRecord.pressure -> R.drawable.ic_curve_arrow_down
                        else -> R.drawable.ic_right_arrow
                    }
                )

                graphView.series.clear()
                //if (graphView.series.isEmpty()) {

                graphView.addSeries(LineGraphSeries(it.map { item ->

                    DataPoint(
                        item.stamp.toDouble(),
                        item.pressure

                    )
                }.toTypedArray()).apply {
                    backgroundColor = context.getColor(R.color.md_blue_500, 0.4f)
                    isDrawBackground = true
                    setCustomPaint(Paint().also { p ->
                        p.style = Paint.Style.STROKE
                        p.strokeJoin = Paint.Join.MITER
                        p.strokeWidth =
                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
                        p.color = context.getColor(R.color.md_blue_300)
                    })
                })

                graphView.viewport.isYAxisBoundsManual = true
                graphView.viewport.isXAxisBoundsManual = true
                graphView.viewport.setMinY(it.minBy { s -> s.pressure }!!.pressure - 3)
                graphView.viewport.setMaxY(it.maxBy { s -> s.pressure }!!.pressure + 3)
                graphView.viewport.setMinX(
                    LocalDateTime.now().minusHours(24)
                        .toEpochSecond(OffsetDateTime.now().offset).toDouble() * 1000
                )
                graphView.viewport.setMaxX(Date().time.toDouble() )
                graphView.viewport.isScalable = true
                graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String =
                        if (!isValueX) {
                            String.format("%.1f", value)
                        } else {
                            SimpleDateFormat("mm", Locale.FRANCE).format(Date(value.toLong()))
                            /*String.format(
                                "%dh",
                                TimeUnit.HOURS.convert(value.toLong() - Date().time, TimeUnit.MILLISECONDS)
                            )*/

                        }

                }
                graphView.animate()
            }
        })

        viewModel.getRecordHistory(from = Dates.yesterday)
    }

    private fun updateBarometer(pressure: Double) {

        val t = (pressure - BAROMETER_RANGE_LOW) / (BAROMETER_RANGE_HIGH - BAROMETER_RANGE_LOW)
        var n = Math.ceil(barometerIcons.size * t).toInt()
        n = Math.max(0, Math.min(n, barometerIcons.size - 1))

        imageView.setImageResource(barometerIcons[n])
    }


    companion object {

        private val BAROMETER_RANGE_LOW = 965f
        private val BAROMETER_RANGE_HIGH = 1035f
        private val BAROMETER_RANGE_SUNNY = 1010f
        private val BAROMETER_RANGE_RAINY = 990f

    }
}
