package zelgius.com.atmirror.shared


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_forecast.*
import kotlinx.android.synthetic.main.layout_forecast.view.*
import zelgius.com.atmirror.shared.entities.json.DarkSky
import zelgius.com.atmirror.shared.entities.json.ForecastData
import zelgius.com.utils.toLocalDateTime
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel
import zelgius.com.atmirror.shared.worker.DarkSkyResult
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
abstract class SharedForecastFragment : Fragment() {

    protected abstract val viewModel : SharedMainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.forecastLiveData.observe(this, Observer {
            recyclerView.adapter = ForecastAdapter(it)
        })

        viewModel.workerStatus.observe(this, Observer {list ->
            list.forEach {
                textView.text =
                    "${SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())} ${it.state}"

                when (it.state) {
                    //ENQUEUED because a PeriodicWork never goes SUCCEEDED, it goes directly to ENQUEUED
                    WorkInfo.State.SUCCEEDED, WorkInfo.State.ENQUEUED -> {
                        textView2.text =
                            "Work Done At ${SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())}"

                        DarkSkyResult.result?.apply {
                            viewModel.forecastLiveData.value = this
                        }

                        /*it.outputData.getString("result").apply {
                            if(this != null) {
                                val forecast = Gson().fromJson(this, DarkSky::class.java)
                                viewModel.forecastLiveData.value = forecast

                            } else textView2.text = textView2.text as String + " but result is null"
                        }*/
                    }

                    WorkInfo.State.FAILED -> textView2.text =
                        "Failed ${SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date())}"

                    else -> {}
                }
            }
        })
    }


    class ForecastAdapter(private val forecast: DarkSky) : RecyclerView.Adapter<ForecastViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder = ForecastViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_forecast,
                parent,
                false
            )
        )

        override fun getItemCount(): Int = Math.min(forecast.daily.data.size, 5)


        override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
            holder.bind(forecast.daily.data[position])
        }

    }

    class ForecastViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(item: ForecastData) {
            SimpleDateFormat("HH.mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Europe/Brussels")
            }

            itemView.forecastImage.setImageResource(
                when (item.icon) {
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
            )

            val date = Date(item.time * 1000).toLocalDateTime(ZoneId.of("Europe/Brussels"))
            val now = Date().toLocalDateTime(ZoneId.of("Europe/Brussels"))
            itemView.textView.text = if (now.dayOfMonth == date.dayOfMonth) {
                itemView.context.getString(R.string.today)
            } else {
                val array = itemView.context.resources.getStringArray(R.array.day_of_the_week)

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


            itemView.forecastTemperatureMax.text = itemView.context.getString(
                R.string.forecast_temperature_format,
                item.temperatureMax,
                DateTimeFormatter.ofPattern("HH:mm").format(
                    Date(item.temperatureMaxTime * 1000).toLocalDateTime(
                        ZoneId.of(
                            "Europe/Brussels"
                        )
                    )
                )
            )

            itemView.forecastTemperatureMin.text = itemView.context.getString(
                R.string.forecast_temperature_format,
                item.temperatureMin,
                DateTimeFormatter.ofPattern("HH:mm").format(
                    Date(item.temperatureMinTime * 1000).toLocalDateTime(
                        ZoneId.of(
                            "Europe/Brussels"
                        )
                    )
                )
            )

            itemView.forecastPrecipitation.text = String.format("%.0f%%", item.precipProbability * 100)
        }
    }

}
