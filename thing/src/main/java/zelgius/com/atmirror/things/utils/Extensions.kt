package zelgius.com.atmirror.things.utils

import com.google.android.things.pio.PeripheralManager
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.round



fun PeripheralManager.scanI2cAvailableAddresses(i2cName: String): List<Int> {
    return (0..127).filter { address ->
        with(openI2cDevice(i2cName, address)) {
            try {
                write(ByteArray(1), 1)
                true
            } catch (e: IOException) {
                false
            } finally {
                close()
            }
        }
    }
}

