package zelgius.com.atmirror.things.drivers.inky

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.SpiDevice
import com.zelgius.driver.eink.InkyColor
import com.zelgius.driver.eink.WHatHAL
import com.zelgius.driver.eink.rotate
import kotlinx.coroutines.delay
import java.io.IOException
import java.nio.ByteBuffer

val TAG = WHatHALThing::class.simpleName

class WHatHALThing(
    private val dc: Gpio,
    private val reset: Gpio,
    private val busy: Gpio,
    private val spi: SpiDevice,
    color: InkyColor = InkyColor.RED_HT
) : WHatHAL(color = color) {

    override fun readBusy(): Boolean = busy.value

    override fun writeReset(high: Boolean) {
        reset.value = high
    }

    override fun writeDC(high: Boolean) {
        dc.value = high
    }

    override fun writeSpi(value: Int) {
        spi.write(byteArrayOf(value.toByte()), 1)
    }

    override fun writeSpi(value: IntArray) {
        val buffer = ByteBuffer.wrap(value.map { it.toByte() }.toByteArray())
        while (buffer.remaining() > 0) {
            with(ByteArray(1024)) {
                val get = kotlin.math.min(buffer.remaining(), 1024)
                buffer.get(this, 0, get)
                spi.write(this, get)

            }
        }
    }

    fun setImage(image: Bitmap, rotation: Int? = null, default: InkyColor = InkyColor.WHITE) {

        val array = Array(image.height) { IntArray(image.width) }
        for (i in 0 until image.height) {
            for (j in 0 until image.width) {
                val p = image.getPixel(j, i)
                array[i][j] = when {
                    Color.red(p) == 255 && Color.green(p) == 0 && Color.blue(p) == 0 -> InkyColor.RED.code
                    Color.red(p) == 0 && Color.green(p) == 255 && Color.blue(p) == 255 -> InkyColor.YELLOW.code
                    Color.red(p) == 0 && Color.green(p) == 0 && Color.blue(p) == 0 -> InkyColor.BLACK.code
                    Color.red(p) == 255 && Color.green(p) == 255 && Color.blue(p) == 255 -> InkyColor.WHITE.code
                    else -> default.code
                }
            }
        }

        (if (rotation != null)
            array.rotate(rotation)
        else array)
            .copyInto(buffer)

    }

    override suspend fun show(wait: Boolean) {
        super.show(wait)
        delay(18000)
    }

    fun close() {

        try {
            busy.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            reset.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            dc.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            spi.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}