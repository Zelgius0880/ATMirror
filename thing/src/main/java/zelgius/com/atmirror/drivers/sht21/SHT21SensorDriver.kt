package zelgius.com.atmirror.drivers.sht21

/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.hardware.Sensor
import android.util.Log
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.userdriver.UserDriverManager
import com.google.android.things.userdriver.sensor.UserSensor
import com.google.android.things.userdriver.sensor.UserSensorDriver
import com.google.android.things.userdriver.sensor.UserSensorReading
import zelgius.com.atmirror.drivers.sht21.SHT21.Companion.MAX_TEMP_C
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_MAX_DELAY_US
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_MAX_RANGE
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_MIN_DELAY_US
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_NAME
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_RESOLUTION
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_VENDOR
import zelgius.com.atmirror.drivers.sht21.SHT21SensorDriver.Companion.DRIVER_VERSION
import zelgius.com.atmirror.utils.scanI2cAvailableAddresses
import java.io.IOException
import java.util.*

private val TAG = SHT21SensorDriver::class.java.simpleName

/**
 * Create a new framework sensor driver connected on the given bus and address.
 * The driver emits [android.hardware.Sensor] with pressure and temperature data when
 * registered.
 * @param bus I2C bus the sensor is connected to.
 * @param address I2C address of the sensor.
 * @throws IOException
 * @see .registerHumiditySensor
 * @see .registerTemperatureSensor
 */

class SHT21SensorDriver(bus: String, address: Int = I2C_ADDRESS) : AutoCloseable {

    private val mDevice: SHT21

    private var mTemperatureUserDriver: TemperatureUserDriver? = null
    private var mHumidityUserDriver: HumidityUserDriver? = null

    private var closed = false


    init {
        //Log.e(TAG, " Addresses ${PeripheralManager.getInstance().scanI2cAvailableAddresses(bus)}")
        mDevice = SHT21(bus, address)
    }
    /**
     * Close the driver and the underlying device.
     * @throws IOException
     */
    override fun close() {
        unregisterTemperatureSensor()
        unregisterHumiditySensor()

        try {
            mDevice.close()
        } finally {
            closed = true
        }

    }

    /**
     * Register a [UserSensor] that pipes temperature readings into the Android SensorManager.
     * @see .unregisterTemperatureSensor
     */
    fun registerTemperatureSensor() {
        if (closed) {
            throw IllegalStateException("cannot register closed driver")
        }

        if (mTemperatureUserDriver == null) {
            mTemperatureUserDriver = TemperatureUserDriver(mDevice)
            UserDriverManager.getInstance().registerSensor(mTemperatureUserDriver!!.userSensor)
        }
    }

    /**
     * Register a [UserSensor] that pipes humidity readings into the Android SensorManager.
     * @see .unregisterHumiditySensor
     */
    fun registerHumiditySensor() {
        if (closed) {
            throw IllegalStateException("cannot register closed driver")
        }

        if (mHumidityUserDriver == null) {
            mHumidityUserDriver = HumidityUserDriver(mDevice)
            UserDriverManager.getInstance().registerSensor(mHumidityUserDriver!!.userSensor)
        }
    }

    /**
     * Unregister the temperature [UserSensor].
     */
    fun unregisterTemperatureSensor() {
        if (mTemperatureUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mTemperatureUserDriver!!.userSensor)
            mTemperatureUserDriver = null
        }
    }

    /**
     * Unregister the humidity [UserSensor].
     */
    fun unregisterHumiditySensor() {
        if (mHumidityUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mHumidityUserDriver!!.userSensor)
            mHumidityUserDriver = null
        }
    }


    companion object {
        private val TAG = "SHT21SensorDriver"

        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        const val DRIVER_VENDOR = "Sensirion"
        const val DRIVER_NAME = "SHT21"
        val DRIVER_MIN_DELAY_US = Math.round(1000000f / SHT21.MAX_FREQ_HZ)
        val DRIVER_MAX_DELAY_US = Math.round(1000000f / SHT21.MIN_FREQ_HZ)
        const val DRIVER_MAX_RANGE = MAX_TEMP_C
        const val DRIVER_RESOLUTION = 0.005f
        //const val DRIVER_POWER = SHT21.MAX_POWER_CONSUMPTION_TEMP_UA / 1000f
        const val DRIVER_VERSION = 1
        const val I2C_ADDRESS = 0x40
    }
}

class TemperatureUserDriver(private val device: SHT21) : UserSensorDriver {

    private var mEnabled: Boolean = false
    private var mUserSensor: UserSensor? = null

    val userSensor: UserSensor?
        get() {
            if (mUserSensor == null) {
                mUserSensor = UserSensor.Builder()
                    .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    .setMaxRange(DRIVER_MAX_RANGE.toFloat())
                    .setResolution(DRIVER_RESOLUTION)
                    //.setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY_US)
                    .setMaxDelay(DRIVER_MAX_DELAY_US)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build()
            }
            return mUserSensor
        }

    override fun read(): UserSensorReading {
        return UserSensorReading(floatArrayOf(device.readTemperature()))
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
        //syncSamplingState()
    }

    fun isEnabled(): Boolean {
        return mEnabled
    }

    companion object {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t

    }
}

class HumidityUserDriver(private val device: SHT21) : UserSensorDriver {

    private var mEnabled: Boolean = false
    private var mUserSensor: UserSensor? = null

    val userSensor: UserSensor?
        get() {
            if (mUserSensor == null) {
                mUserSensor = UserSensor.Builder()
                    .setType(Sensor.TYPE_RELATIVE_HUMIDITY)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    .setMaxRange(DRIVER_MAX_RANGE.toFloat())
                    .setResolution(DRIVER_RESOLUTION)
                    //.setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY_US)
                    .setMaxDelay(DRIVER_MAX_DELAY_US)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build()
            }
            return mUserSensor
        }

    @ExperimentalUnsignedTypes
    override fun read(): UserSensorReading {
        return UserSensorReading(floatArrayOf(device.readHumidity()))
    }

    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
        //syncSamplingState()
    }

    fun isEnabled(): Boolean {
        return mEnabled
    }
}