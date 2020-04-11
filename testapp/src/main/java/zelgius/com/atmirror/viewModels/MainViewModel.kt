package zelgius.com.atmirror.viewModels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import khronos.Dates
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel
import zelgius.com.atmirror.shared.worker.DarkSkyWorker
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


class MainViewModel(private val context: Application) : SharedMainViewModel(context) {

    /**
     * @param data a record. If the minutes of stamp is 0, it will be saved in database
     * @return true if saved in database, false otherwise
     */
    val rnd = Random()

    init {
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

    override fun updateLastKnownRecord(data: zelgius.com.atmirror.shared.entities.SensorRecord): Boolean {
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
                lastKnownRecord.temperature = rnd.nextDouble() * 100

            if (data.humidityPresent)
                lastKnownRecord.humidity = data.humidity


            if (true) {// new hour -> saving the record in database
                viewModelScope.launch {
                    databaseService.insertRecord(lastKnownRecord)
                    history.postValue(databaseService.blockingGetSensorDataHistory(from = Dates.yesterday).asReversed())
                }
            }

            return true
        }

        return false
    }
}