package zelgius.com.atmirror.viewModels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.*
import androidx.work.*
import khronos.Dates
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver
import zelgius.com.atmirror.entities.SensorRecord
import zelgius.com.atmirror.entities.UnknownSignal
import zelgius.com.atmirror.entities.json.OpenWeatherMap
import zelgius.com.atmirror.repositories.OpenWeatherMapRepository
import zelgius.com.atmirror.repositories.DatabaseRepository
import zelgius.com.atmirror.shared.repositories.PiclockRepository
import zelgius.com.atmirror.worker.*
import zelgius.com.utils.toLocalDateTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


class MainViewModel (private val context: Application) : AndroidViewModel(context), SensorEventListener {
    private val sensorCallback = SensorCallback()

    private val piclockService = PiclockRepository()
    private  val databaseService = DatabaseRepository(context)

    val piclockCurrentRecord by lazy { piclockService.listenCurrentRecord() }
    private val lastKnownRecord = SensorRecord()

    val history = MutableLiveData<List<SensorRecord>>()
    val sensorManager by lazy { (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager) }
    private  var temperatureSensor: Sensor? = null
    val sht21Record = MutableLiveData<SensorRecord>()
    var forecastLiveData = MutableLiveData<OpenWeatherMap>()
    var netatmoLiveData = MutableLiveData<List<Pair<LocalDateTime, Double>>>()

    private var workerOwmState: LiveData<Operation.State>
    var workerOwmStatus: LiveData<List<WorkInfo>>

    private var workerNetatmoState: LiveData<Operation.State>
    var workerNetatmoStatus: LiveData<List<WorkInfo>>

    init {
        SHT21SensorDriver("I2C1").apply {
            registerTemperatureSensor()
            registerHumiditySensor()
        }

        //WorkManager.getInstance().cancelAllWork()
        OpenWeatherMapRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).enqueue(object  :
            Callback<OpenWeatherMap> {
            override fun onFailure(call: Call<OpenWeatherMap>, t: Throwable) {}

            override fun onResponse(call: Call<OpenWeatherMap>, response: Response<OpenWeatherMap>) {
                if(forecastLiveData.value == null) forecastLiveData.value = response.body()
            }

        })

        val periodicOwmWorker = PeriodicWorkRequestBuilder<DarkSkyWorker>(Duration.ofHours(1))
            .setConstraints(Constraints.NONE)
            .addTag("darkSkyRequest")
            .build()

        workerOwmState = WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("darkSkyRequest", ExistingPeriodicWorkPolicy.REPLACE, periodicOwmWorker)
            .state

        workerOwmStatus = WorkManager.getInstance(context).getWorkInfosByTagLiveData("darkSkyRequest")

        val periodicNetatmoWorker = PeriodicWorkRequestBuilder<NetatmoWorker>(Duration.ofMinutes(16))
            .setConstraints(Constraints.NONE)
            .addTag("netatmoRequest")
            .build()

        workerNetatmoState = WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("netatmoRequest", ExistingPeriodicWorkPolicy.REPLACE, periodicNetatmoWorker)
            .state

        workerNetatmoStatus = WorkManager.getInstance(context).getWorkInfosByTagLiveData("netatmoRequest")

        val midnight = Calendar.getInstance().run {
            add(Calendar.DATE, 1)
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            time.time - Date().time
        }

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("reboot", ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<RebootWorker>(Duration.ofHours(24))
                    .setConstraints(Constraints.NONE)
                    .addTag("reboot")
                    .setInitialDelay(Duration.ofMillis(midnight))
                    .build()
            )
            .state

    }

    fun registerDrivers() {
        sensorManager.registerDynamicSensorCallback(sensorCallback)
        temperatureSensor = sensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).firstOrNull()

        if (temperatureSensor != null)
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterDrivers() {
        sensorManager.unregisterDynamicSensorCallback(sensorCallback)
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        println("Accuracy changed: $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        //println("Sensor changed: ${event.sensor.stringType} ${event.values[0]}")
        val record = sht21Record.value ?: SensorRecord()
        record.stamp = Date().time
        when (event.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                record.temperature = event.values[0].toDouble()

                viewModelScope.launch {
                    databaseService.updateRecordWithTemperature(record.stamp, record.temperature)
                }
            }
            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                record.humidity = event.values[0].toDouble()

                viewModelScope.launch {
                    databaseService.updateRecordWithRh(record.stamp, record.humidity)
                }
            }
        }

        sht21Record.value = record
    }


    /**
     * @param data a record. If the minutes of stamp is 0, it will be saved in database
     * @return true if saved in database, false otherwise
     */

    fun updateLastKnownRecord(data: SensorRecord): Boolean {
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


            if (data.date.time.toLocalDateTime().hour != old.hour) {// new hour -> saving the record in database
                viewModelScope.launch {
                    databaseService.insertRecord(lastKnownRecord)
                    history.postValue(databaseService.blockingGetSensorDataHistory(from = Dates.yesterday).asReversed())
                }
            }

            return true
        }

        return false
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


    fun saveUnknownSignal(item: UnknownSignal): LiveData<Long> =
        MutableLiveData<Long>().also {
            viewModelScope.launch {
                with(databaseService.insertUnknownSignal(item)) {
                    it.value = this
                }
            }
        }



    internal inner class SensorCallback : SensorManager.DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
            // Begin listening for sensor readings
            sensorManager.registerListener(
                this@MainViewModel, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        override fun onDynamicSensorDisconnected(sensor: Sensor) {
            // Stop receiving sensor readings
            sensorManager.unregisterListener(this@MainViewModel)
        }
    }

}