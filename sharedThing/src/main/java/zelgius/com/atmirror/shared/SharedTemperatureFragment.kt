package zelgius.com.atmirror.shared


import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.fragment_temperature.*
import kotlinx.android.synthetic.main.fragment_temperature.graphView
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SharedTemperatureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


abstract class SharedTemperatureFragment : Fragment() {
    val TAG = SharedTemperatureFragment::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    protected abstract val viewModel: SharedMainViewModel

    private val mHandler = Handler()
    private lateinit var mTimer: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temperature, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//50.092240, 5.323880

        viewModel.sht21Record.observe(this, Observer {
            //            textView.text =                 "${getString(R.string.temperature_title)} ${SimpleDateFormat("HH:mm", Locale.FRANCE).format(it.date)}"


            temperature.text = String.format("%.1f °C", it.temperature)
        })

        viewModel.history.observe(this, Observer {
            /*textView.text =
                "${getString(R.string.temperature_title)} ${SimpleDateFormat("dd HH:mm", Locale.FRANCE).format(Date())}"*/
            if (it.isNotEmpty()) {
                /*textView.text = "${getString(R.string.temperature_title)} ${it.size} ${SimpleDateFormat(
                    "dd HH:mm",
                    Locale.FRANCE
                ).format(it.last().date)}"*/

                graphView.series.clear()
                //if (graphView.series.isEmpty()) {

                graphView.addSeries(LineGraphSeries(it.map { item ->

                    DataPoint(
                        item.stamp.toDouble(),
                        item.temperature

                    )
                }.toTypedArray()).apply {
                    setCustomPaint(Paint().also { p ->
                        p.style = Paint.Style.STROKE
                        p.strokeJoin = Paint.Join.MITER
                        p.strokeWidth =
                            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
                        p.color = context!!.getColor(R.color.md_red_300)
                    })
                })

                graphView.viewport.isYAxisBoundsManual = true
                graphView.viewport.isXAxisBoundsManual = true
                graphView.viewport.setMinY(it.minBy { s -> s.temperature }!!.temperature - 3)
                graphView.viewport.setMaxY(it.maxBy { s -> s.temperature }!!.temperature + 3)
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
                            String.format(
                                "%dh",
                                TimeUnit.HOURS.convert(value.toLong() - Date().time, TimeUnit.MILLISECONDS)
                            )

                        }

                }
                graphView.animate()

                /*} else {
                    (graphView.series.first() as LineGraphSeries).resetData(it
                        .subList(if(it.size - 24 - 1 < 0) 0 else it.size - 24 - 1, it.size )
                        .map { item ->
                            DataPoint(
                                TimeUnit.HOURS.convert(item.stamp - Dates.today.time, TimeUnit.MILLISECONDS).toDouble(),
                                item.temperature
                            )
                        }.toTypedArray())
                    graphView.animate()
                }*/
            }
        })

        viewModel.getRecordHistory(from = Dates.yesterday)


        /*mTimer = object : Runnable {
            override fun run() {
                newSerie()
                mHandler.postDelayed(this, 330)
            }
        }
        mHandler.postDelayed(mTimer, 1500)*/
    }

    fun newSerie() {
        graphView.series.clear()
        //if (graphView.series.isEmpty()) {

        val list = mutableListOf<DataPoint>()
        val rnd = Random()
        for (i in 24 downTo 0) {
            list.add(DataPoint(0.0 - i, rnd.nextInt().toDouble()))
        }
        graphView.addSeries(LineGraphSeries(list.toTypedArray()).apply {
            setCustomPaint(Paint().also { p ->
                p.style = Paint.Style.STROKE
                p.strokeJoin = Paint.Join.ROUND
                p.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
                p.color = context!!.getColor(R.color.md_red_300)
            })
        })


        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinY(list.minBy { s -> s.y }!!.y - 3)
        graphView.viewport.setMaxY(list.maxBy { s -> s.y }!!.y + 3)
        graphView.viewport.setMinX(-4.0)
        graphView.viewport.setMaxX(0.0)
        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String =
                if (!isValueX) String.format("%.1f", value)
                else String.format("%.0fh", value)

        }
        graphView.animate()
    }
}
