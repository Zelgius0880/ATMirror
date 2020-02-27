package zelgius.com.shared.viewModels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import khronos.Dates
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zelgius.com.shared.entities.json.DarkSky
import zelgius.com.shared.repositories.DarkSkyRepository
import zelgius.com.shared.repositories.DatabaseRepository
import zelgius.com.shared.repositories.PiclockRepository
import zelgius.com.utils.toLocalDateTime
import zelgius.com.shared.worker.DarkSkyWorker
import zelgius.com.shared.worker.KEY
import zelgius.com.shared.worker.LATITUDE
import zelgius.com.shared.worker.LONGITUDE
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


open class SharedMainViewModel(private val context: Application) : AndroidViewModel(context) {


    val piclockService = PiclockRepository()
    protected  val databaseService = DatabaseRepository(context)

    val piclockCurrentRecord by lazy { piclockService.listenCurrentRecord() }
    val lastKnownRecord = zelgius.com.shared.entities.SensorRecord()

    val history = MutableLiveData<List<zelgius.com.shared.entities.SensorRecord>>()
    val sensorManager by lazy { (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager) }
    protected  var temperatureSensor: Sensor? = null
    val sht21Record = MutableLiveData<zelgius.com.shared.entities.SensorRecord>()
    var forecastLiveData = MutableLiveData<DarkSky>()

    var workerState: LiveData<Operation.State>
    var workerStatus: LiveData<List<WorkInfo>>


    init {
        //WorkManager.getInstance().cancelAllWork()
        DarkSkyRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).enqueue(object  : Callback<DarkSky> {
            override fun onFailure(call: Call<DarkSky>, t: Throwable) {}

            override fun onResponse(call: Call<DarkSky>, response: Response<DarkSky>) {
                if(forecastLiveData.value == null) forecastLiveData.value = response.body()
            }

        })

        val periodicWorker = PeriodicWorkRequestBuilder<DarkSkyWorker>(Duration.ofHours(1))
            .setConstraints(Constraints.NONE)
            .addTag("darkSkyRequest")
            .build()

        workerState = WorkManager
            .getInstance()
            .enqueueUniquePeriodicWork("darkSkyRequest", ExistingPeriodicWorkPolicy.REPLACE, periodicWorker)
            .state

        workerStatus = WorkManager.getInstance().getWorkInfosByTagLiveData("darkSkyRequest")
    }

    override fun onCleared() {
        super.onCleared()

        piclockService.unlistenCurrentRecord()

    }

    fun getRecordHistory(from: Date) {
        viewModelScope.launch {
            databaseService.blockingGetSensorDataHistory(from).let {
                if (it.isEmpty()) {
                    piclockService.getSensorDataHistory(
                        from = from.time,
                        to = Date().time
                    ) { list ->
                        viewModelScope.launch {
                            databaseService.insertRecord(*list.toTypedArray())
                            history.postValue(list)
                        }
                    }
                } else
                    history.postValue(it.asReversed())
            }
        }
    }


    /**
     * @param data a record. If the minutes of stamp is 0, it will be saved in database
     * @return true if saved in database, false otherwise
     */

    open fun updateLastKnownRecord(data: zelgius.com.shared.entities.SensorRecord): Boolean {
        if (data.stamp > lastKnownRecord.stamp) {

            val old = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastKnownRecord.stamp),
                TimeZone.getDefault().toZoneId())

            lastKnownRecord.stamp = data.stamp

            if (data.altitudePresent)
                lastKnownRecord.altitude = data.altitude

            if (data.pressurePresent)
                lastKnownRecord.pressure = data.pressure

            if (data.temperaturePresent)
                lastKnownRecord.temperature = data.temperature

            if (data.humidityPresent)
                lastKnownRecord.humidity = data.humidity


            if (data.date.toLocalDateTime().hour != old.hour) {// new hour -> saving the record in database
                viewModelScope.launch {
                    databaseService.insertRecord(lastKnownRecord)
                    history.postValue(databaseService.blockingGetSensorDataHistory(from = Dates.yesterday).asReversed())
                }
            }

            return true
        }

        return false
    }

    open fun registerDrivers() {
    }

    open fun unregisterDrivers() {
    }
}