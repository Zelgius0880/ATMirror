package zelgius.com.atmirror.things.viewModels

import androidx.datastore.core.DataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zelgius.com.atmirror.shared.repository.MeasureType
import zelgius.com.atmirror.shared.repository.NetatmoResult
import zelgius.com.atmirror.things.entities.SensorRecord
import zelgius.com.atmirror.things.entities.json.ForecastData
import zelgius.com.atmirror.things.entities.json.OpenWeatherMap
import zelgius.com.atmirror.things.proto.OpenWeatherMapProto
import zelgius.com.atmirror.things.protobuf.NetatmoResultProto
import zelgius.com.atmirror.things.protobuf.toEntity
import zelgius.com.atmirror.things.protobuf.toProto
import zelgius.com.atmirror.things.repositories.OpenWeatherMapRepository
import zelgius.com.atmirror.things.worker.KEY
import zelgius.com.atmirror.things.worker.LATITUDE
import zelgius.com.atmirror.things.worker.LONGITUDE
import zelgius.com.atmirror.things.worker.NetatmoWorker
import zelgius.com.atmirror.things.worker.OpenWeatherMapWorker
import zelgius.com.atmirror.things.worker.RebootWorker
import java.time.Duration
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val forecastDataStore: DataStore<OpenWeatherMapProto>,
    @Named("Inside") private val insideNetatmoDataStore: DataStore<NetatmoResultProto>,
    @Named("Outside") private val outsideNetatmoDataStore: DataStore<NetatmoResultProto>,
    openWeatherMapRepository: OpenWeatherMapRepository
) : ViewModel() {

    val history: LiveData<List<SensorRecord>> = insideNetatmoDataStore.data.map { it.toEntity() }
        .map {
            buildList {
                for (i in 0 until (it.values.firstOrNull()?.size?:0)) {
                    add(
                        SensorRecord().apply {
                            humidity = it[MeasureType.Humidity]?.get(i)?.data ?: 0.0
                            temperature = it[MeasureType.Temperature]?.get(i)?.data ?: 0.0
                            pressure = it[MeasureType.Pressure]?.get(i)?.data ?: 0.0
                            stamp =
                                it[MeasureType.Temperature]?.get(i)?.time?.toInstant(ZoneOffset.UTC)
                                    ?.toEpochMilli() ?: 0L
                        }
                    )
                }
            }
        }.asLiveData(Dispatchers.Main)

    val currentInsideRecord: LiveData<SensorRecord> =
        insideNetatmoDataStore.data.map { it.toEntity() }
            .map {
                SensorRecord().apply {
                    humidity = it[MeasureType.Humidity]?.maxByOrNull { it.time }?.data ?: 0.0
                    temperature = it[MeasureType.Temperature]?.maxByOrNull { it.time }?.data ?: 0.0
                    pressure = it[MeasureType.Pressure]?.maxByOrNull { it.time }?.data ?: 0.0
                    stamp =
                        it[MeasureType.Temperature]?.maxOf { it.time }?.toInstant(ZoneOffset.UTC)
                            ?.toEpochMilli() ?: 0L
                }
            }.asLiveData(Dispatchers.Main)

    val forecastLiveData: LiveData<List<ForecastData>> =
        forecastDataStore.data.asLiveData(Dispatchers.Main).map {
            it.forecastList.map { f -> f.toEntity() }
        }

    val outsideLiveData: LiveData<Map<MeasureType, List<NetatmoResult>>> =
        outsideNetatmoDataStore.data.asLiveData(Dispatchers.Main).map {
            it.toEntity()
        }

    init {
        //WorkManager.getInstance().cancelAllWork()
        openWeatherMapRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).enqueue(object :
            Callback<OpenWeatherMap> {
            override fun onFailure(call: Call<OpenWeatherMap>, t: Throwable) {}

            override fun onResponse(
                call: Call<OpenWeatherMap>,
                response: Response<OpenWeatherMap>
            ) {
                if (forecastLiveData.value == null)
                    viewModelScope.launch {
                        forecastDataStore.updateData {
                            response.body()?.toProto() ?: it
                        }
                    }

            }
        })

        startOpenWeatherMapWork()
        startNetatmoWorker(true)
        startNetatmoWorker(false)


        val midnight = Calendar.getInstance().run {
            add(Calendar.DATE, 1)
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            time.time - Date().time
        }

        workManager
            .enqueueUniquePeriodicWork(
                "reboot", ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<RebootWorker>(Duration.ofHours(24))
                    .setConstraints(Constraints.NONE)
                    .addTag("reboot")
                    .setInitialDelay(Duration.ofMillis(midnight))
                    .build()
            )
            .state
    }

    private fun startOpenWeatherMapWork() {
        val periodicOwmWorker =
            PeriodicWorkRequestBuilder<OpenWeatherMapWorker>(Duration.ofHours(1))
                .setConstraints(Constraints.NONE)
                .addTag("openWeatherMap")
                .build()

        workManager
            .enqueueUniquePeriodicWork(
                "openWeatherMap",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicOwmWorker
            )
    }

    private fun startNetatmoWorker(inside: Boolean) {
        val outsideNetatmoWorker =
            PeriodicWorkRequestBuilder<NetatmoWorker>(Duration.ofMinutes(20))
                .setConstraints(Constraints.NONE)
                .setInputData(Data.Builder().putBoolean("inside", inside).build())
                .addTag("netatmo${if (inside) "Inside" else "Outside"}Request")
                .build()

        workManager
            .enqueueUniquePeriodicWork(
                "netatmo${if (inside) "Inside" else "Outside"}Request",
                ExistingPeriodicWorkPolicy.REPLACE,
                outsideNetatmoWorker
            )
    }
}