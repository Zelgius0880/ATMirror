package zelgius.com.atmirror.viewModels

import android.app.Application
import android.graphics.*
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.zelgius.driver.eink.output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.atmirror.compose.Screen1
import zelgius.com.atmirror.compose.Screen2
import zelgius.com.atmirror.drivers.inky.WHatHALThing
import zelgius.com.atmirror.openIutput
import zelgius.com.atmirror.openOutput
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


val TAG = InkyViewModel::class.simpleName

@SuppressWarnings("")
class InkyViewModel(private val app: Application) : AndroidViewModel(app) {

    private val dc: Gpio = PeripheralManager.getInstance().openOutput("BCM22")
    private val queue = Channel<DisplayQueue>(Channel.CONFLATED/*.UNLIMITED*/)

    private val inky2 by lazy {
        with(PeripheralManager.getInstance()) {
            WHatHALThing(
                dc = dc,
                cs = openOutput("BCM26"),//26
                reset = openOutput("BCM19"),
                busy = openIutput("BCM6"),
                spi = spi
            )
        }
    }

    private val inky1 by lazy {
        with(PeripheralManager.getInstance()) {
            WHatHALThing(
                dc = dc,
                cs = openOutput("BCM20"), //20
                reset = openOutput("BCM16"),
                busy = openIutput("BCM5"),
                spi = spi
            )
        }
    }


    private val spi by lazy {
        val manager = PeripheralManager.getInstance()
        manager.openSpiDevice("SPI0.0")
    }

    private var lastS1: Screen1? = null
    private var lastS2: Screen2? = null
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                /* inky1.clean()
                 inky2.clean()*/
                while (true) {
                    val (s1, s2) = queue.receive()

                    val task1 = if (s1 != lastS1 && s1 != null && s1.bitmap != null) {
                        async {
                            lastS1 = s1
                            inky1.setImage(s1.bitmap!!.rotate(-90f))
                        }
                    } else null

                    val task2 = if (s2 != lastS2 && s2 != null && s2.bitmap != null) async {
                        lastS2 = s2
                        inky2.setImage(s2.bitmap!!.rotate(90f))
                    } else null

                    task1?.await()
                    task2?.await()

                    if (task1 != null) inky1.show(true)
                    if (task2 != null) inky2.show(true)
                }
            }
        }
    }

    fun display(screen1: Screen1? = null, screen2: Screen2? = null) {
        viewModelScope.launch {
            queue.send(DisplayQueue(screen1?: lastS1, screen2?: lastS2))
        }
    }


    override fun onCleared() {
        super.onCleared()
        inky1.close()
        inky2.close()
    }


    private fun Bitmap.rotate(degrees: Float = 90f): Bitmap =
        Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            Matrix().apply { postRotate(degrees) },
            false
        )
            .apply {
                try {
                    FileOutputStream(
                        File(
                            app.getExternalFilesDir("images"),
                            "bmp_rotated.png"
                        )
                    ).use { out ->
                        compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
}

data class DisplayQueue(
    val screen1: Screen1?,
    val screen2: Screen2?
)

fun View.toBitmap(
    totalWidth: Int = measuredWidth,
    totalHeight: Int = measuredHeight,
    rect: Rect
): Bitmap {
    setLayerType(View.LAYER_TYPE_SOFTWARE, Paint().apply { isAntiAlias = false })
    val b = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val c = Canvas(b).apply {
        drawFilter = PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG, 0)
    }
    measure(totalWidth, totalHeight)
    layout(0, 0, totalHeight, totalWidth)

    draw(c)

    try {
        FileOutputStream(File(context.getExternalFilesDir("images"), "bmp.png")).use { out ->
            b.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return Bitmap.createBitmap(b, rect.left, rect.top, rect.width(), rect.height())
}

fun Bitmap.scale(dstWidth: Int, dstHeight: Int): Bitmap =
    Bitmap.createScaledBitmap(this, dstWidth, dstHeight, false)