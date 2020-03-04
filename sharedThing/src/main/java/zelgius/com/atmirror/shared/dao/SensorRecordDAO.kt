package zelgius.com.atmirror.shared.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import zelgius.com.atmirror.shared.entities.SensorRecord

@Dao
interface SensorRecordDAO{
    @Insert
    fun insert(vararg record: SensorRecord): List<Long>

    @Delete
    fun delete(record: SensorRecord)

    @Update
    fun update(record: SensorRecord)


    @Query("SELECT * FROM SensorRecord WHERE stamp >= :from AND stamp <= :to ORDER BY stamp DESC")
    fun get(from: Long, to: Long): LiveData<List<SensorRecord>>


    @Query("SELECT * FROM SensorRecord WHERE stamp >= :from AND stamp <= :to ORDER BY stamp DESC")
    fun blockingGet(from: Long, to: Long): List<SensorRecord>

    @Query("UPDATE SensorRecord SET humidity = :rh, humidityPresent = 1 WHERE strftime('%Y-%m-%d %H:%M',datetime(stamp/1000, 'unixepoch', 'localtime')) = strftime('%Y-%m-%d %H:%M',datetime(:stamp/1000, 'unixepoch', 'localtime')) ")
    fun blockingUpdateRh(stamp: Long, rh: Double)

    @Query("UPDATE SensorRecord SET temperature = :temperature, temperaturePresent = 1 WHERE strftime('%Y-%m-%d %H:%M',datetime(stamp/1000, 'unixepoch', 'localtime')) = strftime('%Y-%m-%d %H:%M',datetime(:stamp/1000, 'unixepoch', 'localtime'))")
    fun blockingUpdateTemperature(stamp: Long, temperature: Double)
}