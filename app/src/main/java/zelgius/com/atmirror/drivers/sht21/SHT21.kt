package zelgius.com.atmirror.drivers.sht21

import android.util.Log
import com.google.android.things.pio.I2cDevice
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import com.google.android.things.pio.PeripheralManager
import java.io.IOException
import java.util.concurrent.Semaphore
import kotlin.experimental.and
import kotlin.experimental.or


/**
 * Implementation of {@link de.freitag.stefan.sht21.SHT21}.
 */
private val TAG = SHT21::class.java.simpleName

class SHT21(i2cName: String, address: Int = 0x60) {

    companion object{
        const val MAX_TEMP_C = 125
        const val MIN_TEMP_C = -40
        const val MAX_HUM_RH = 100
        const val MAX_FREQ_HZ = 2
        const val MIN_FREQ_HZ = 1
    }
    /**
     * The SHT21 as I2C device.
     */
    private val pioService = PeripheralManager.getInstance()
    private val device: I2cDevice = pioService.openI2cDevice(i2cName, address)
    private val semaphore = Semaphore(1)


    init {
        softReset()
    }
    /**
     * Sleep for a certain amount of time.
     *
     * @param milliseconds Milliseconds to sleep.
     * @throws IllegalArgumentException if {@code milliseconds} is less than zero.
     */
    private fun delay(milliseconds: Long) {
        assert(milliseconds >= 0)
        if (milliseconds == 0L) {
            return
        }
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    /**
     * Calculates the relative humidity.
     * The relative humidity RH is obtained by the following formula (result in
     * %RH), no matter which resolution is chosen:
     * RH = - 6 + 125 x (S_RH/2^16)
     *
     * @param buffer containing two bytes (humidity raw value, 16bit scaled)
     * @return relative humidity [%RH]
     */
    @kotlin.ExperimentalUnsignedTypes
    fun calcRH(buffer: ByteBuffer): Float {
        /*var sRH = (buffer.get(0).toUInt() and 0xFF.toUInt())

        sRH = sRH shl 8
        sRH = sRH or (buffer.get(1).toUInt()  and 0xFF.toUInt())*/

        var sRH = buffer.getShort(0).toUShort().toInt()//.toFloat()
        // clear bits [1..0] (status bits)
        //sRH = sRH  and 0xFFFC //  ~0x0003
        //sRH = sRH and 0xffff
        //sRH = sRH shr 2
        return (-6 + 125 * sRH.toFloat() / Math.pow(2.0, 16.0)).toFloat() - 10

    }

    /**
     * Calculates temperature.
     *
     * @param buffer Buffer containing two bytes (temperature raw value, 16bit scaled)
     * @return temperature [deg C]
     */
    fun calcTemperatureC(buffer: ByteBuffer): Float {
        var sTemp = buffer.getShort(0)
        sTemp  = sTemp and 0xFFFC.toShort() //   ~0x0003
        return -46.85f + 175.72f / 65536 * sTemp
    }

    /**
     * Calculates checksum for n bytes of data and compares it with expected checksum.
     *
     * @param data checksum is built based on this data
     * @return {@code true}, if calculated and reported checksum match. Otherwise, {@code false}
     * is returned.
     */
    /*u8t SHT2x_CheckCrc(u8t data[], u8t nbrOfBytes, u8t checksum)
    //==============================================================================
    {
        u8t crc = 0;
        u8t byteCtr;
        //calculates 8-Bit checksum with given polynomial
        for (byteCtr = 0; byteCtr < nbrOfBytes; ++byteCtr)
        { crc ^= (data[byteCtr]);
            for (u8t bit = 8; bit > 0; --bit)
            { if (crc & 0x80) crc = (crc << 1) ^ POLYNOMIAL;
                else crc = (crc << 1);
            }
        }
        if (crc != checksum) return CHECKSUM_ERROR;
        else return 0;
    }*/

    fun checkCrC(data: ByteArray): Boolean {
        val POLYNOMIAL = 0x131
        var crc = 0
        val checksum = data[2]

        for (byteCtr in 0 until data.size -1 ) {
            crc = crc xor data[byteCtr].toInt()
            for (bit in 8 downTo 1) {
                crc = if ((crc and 0x80) == 0x80) {
                    (crc shl  1) xor  POLYNOMIAL
                } else {
                    crc shl  1
                }
            }
        }

        return if (crc.toByte() == checksum) {
            //Log.d(TAG, "Checksum matches")
            true
        } else {
            Log.d(TAG, "Checksum does not match. Expected : $checksum. Calculated: $crc")
            false
        }
    }

    /**
     * Performs a reset.
     */
    fun softReset() {
        try {
            Log.d(
                TAG,
                "Writing byte " + String.format(
                    "0x%02X",
                    Command.SOFT_RESET.commandByte
                ) + " to device " + this.device
            )
            this.device.write(byteArrayOf(Command.SOFT_RESET.commandByte), 1)
        } catch (exception: IOException) {
            Log.e("SoftReset failed with an IOException: ", exception.message)
        }
        delay(50)
    }



    fun getResolution(): Resolution? {
        this.softReset()
        try {
            Log.d(TAG,
                "Writing byte " + String.format(
                    "0x%02X",
                    Command.USER_REG_R.commandByte
                ) + " to device " + this.device
            )
            this.device.write(byteArrayOf(Command.USER_REG_R.commandByte), 1)
            delay(100)
            val bytes = ByteArray(1)
            this.device.read(bytes, 1)
            return Resolution.getResolution(bytes[0])
        } catch (exception: IOException) {
            Log.e("Getting resolution failed because of an IOException: ", exception.message);
        }
        return null
    }



    fun getEndOfBatteryAlert(): EndOfBatteryAlert? {
        Log.d(TAG, "Starting getEndOfBatteryAlert()")
        this.softReset()
        try {
            Log.d(
                TAG,
                "Writing byte " + String.format(
                    "0x%02X",
                    Command.USER_REG_R.commandByte
                ) + " to device " + this.device
            );
            this.device.write(byteArrayOf(Command.USER_REG_R.commandByte), 1)
            delay(100)

            val bytes = ByteArray(1)
            this.device.read(bytes, 1)
            return EndOfBatteryAlert.getEOBAlert(bytes[0])
        } catch (exception: IOException) {
            Log.e("getEOBAlert() failed.", exception.message)
        }
        return null
    }

    fun getHeaterStatus(): HeaterStatus? {
        this.softReset()
        try {
            Log.d(
                TAG,
                "Writing byte " + String.format(
                    "0x%02X",
                    Command.USER_REG_R.commandByte
                ) + " to device " + this.device
            )

            this.device.write(byteArrayOf(Command.USER_REG_R.commandByte), 1)
            delay(100)

            val bytes = ByteArray(1)
            this.device.read(bytes, 1)
            return HeaterStatus.getStatus(bytes[0])
        } catch (exception: IOException) {
            Log.e("Getting heater status failed. IOException: ", exception.message)
        }
        return null
    }

    fun measurePoll(measureType: MeasureType) =
        when (measureType) {
            MeasureType.HUMIDITY -> Measurement.create(this.measurePollHumidity(), MeasureType.HUMIDITY)
            MeasureType.TEMPERATURE -> Measurement.create(this.measurePollTemperature(), MeasureType.TEMPERATURE)
        }

    fun readTemperature() = measurePollTemperature()
    fun readHumidity() = measurePollHumidity()

    private fun measurePollTemperature(): Float {
        //this.softReset()
        try {
            semaphore.acquire()
            this.device.write(byteArrayOf(Command.TRIG_T_MEASUREMENT_POLL.commandByte), 1)
            delay(260)
            val bytes = ByteArray(3)
            this.device.read(bytes, 3)
            val buffer = ByteBuffer.allocate(2)
            buffer.order(ByteOrder.BIG_ENDIAN)
            buffer.put(bytes[0])
            buffer.put(bytes[1] and 0xFC.toByte())

            semaphore.release()
            return if (checkCrC(bytes)) {
                calcTemperatureC(buffer)
            } else {
                Float.MIN_VALUE
            }

        } catch (exception: IOException) {
            Log.e("Temperature measurement failed because of an IOException: ", exception.message)
            return Float.MIN_VALUE
        }
    }

    private fun measurePollHumidity(): Float {
        //this.softReset()
        try {
            semaphore.acquire()
            //Log.e(TAG, "Resolution is ${getResolution()}")

            this.device.write(byteArrayOf(Command.TRIG_RH_MEASUREMENT_HM.commandByte), 1)
            delay(260)
            val bytes = ByteArray(3)
            this.device.read(bytes, 3)
            val buffer = ByteBuffer.allocate(2)
            buffer.order(ByteOrder.BIG_ENDIAN)
            buffer.put(bytes[0])
            buffer.put((bytes[1] and 0xFC.toByte()))

            semaphore.release()
            return if (checkCrC(bytes)) {
                calcRH(buffer).also {
                    //Log.e(TAG, "RH is $it")
                }
            } else {
                Float.MIN_VALUE
            }

        } catch (exception: IOException) {
            Log.e("Humidity measurement failed because of an IOException: ", exception.message)
            return Float.MIN_VALUE
        }
    }

    fun close() {
        device.close()
    }

}
