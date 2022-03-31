package zelgius.com.atmirror.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import zelgius.com.atmirror.AppDatabase
import zelgius.com.atmirror.entities.SensorRecord
import java.util.Date

class DatabaseRepository(val context: Context) {
    private val db = AppDatabase.getInstance(context)

    fun getSensorDataHistory(from: Long, to: Long = Date().time): LiveData<List<SensorRecord>> =
        db.sensorRecordDao.get(from, to)

    fun getSensorDataHistory(from: Date, to: Date = Date()): LiveData<List<SensorRecord>> =
        getSensorDataHistory(from.time, to.time)

    suspend fun insertRecord(vararg sensorRecord: SensorRecord): List<Long> {
        return withContext(IO) {
             db.sensorRecordDao.insert(*sensorRecord)
        }
    }


    suspend fun blockingGetSensorDataHistory(from: Long, to: Long = Date().time): List<SensorRecord> =
        withContext(IO) {db.sensorRecordDao.blockingGet(from, to)}

    suspend fun blockingGetSensorDataHistory(from: Date, to: Date = Date()): List<SensorRecord> =
        blockingGetSensorDataHistory(from.time, to.time)

    suspend fun updateRecordWithRh(stamp: Long, rh: Double){
        return withContext(IO){
            db.sensorRecordDao.blockingUpdateRh(stamp, rh)
        }
    }

    suspend fun updateRecordWithTemperature(stamp: Long, temperature: Double){
        return withContext(IO){
            db.sensorRecordDao.blockingUpdateTemperature(stamp, temperature)
        }
    }

}