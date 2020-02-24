package zelgius.com.atmirror.viewModels

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver
import zelgius.com.shared.viewModels.SharedMainViewModel
import java.util.*


class MainViewModel(private val context: Application) : SharedMainViewModel(context), SensorEventListener {
    private val sensorCallback = SensorCallback()



    val sht21 = SHT21SensorDriver("I2C1").apply {
        registerTemperatureSensor()
        registerHumiditySensor()
    }


    override fun registerDrivers() {
        sensorManager.registerDynamicSensorCallback(sensorCallback)
        temperatureSensor = sensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).firstOrNull()

        if (temperatureSensor != null)
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun unregisterDrivers() {
        sensorManager.unregisterDynamicSensorCallback(sensorCallback)
        sensorManager.unregisterListener(this)
    }



    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        println("Accuracy changed: $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        //println("Sensor changed: ${event.sensor.stringType} ${event.values[0]}")
        val record = sht21Record.value ?: zelgius.com.shared.entities.SensorRecord()
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