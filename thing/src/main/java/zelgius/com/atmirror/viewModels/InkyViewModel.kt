package zelgius.com.atmirror.viewModels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.zelgius.bitmap_ktx.floydSteinbergDithering
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
import com.zelgius.bitmap_ktx.rotate
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
                            inky1.setImage(s1.bitmap!!
                                .floydSteinbergDithering(intArrayOf(Color.WHITE, Color.BLACK, Color.RED), Rect(0, 0,300, 200))
                                .floydSteinbergDithering(intArrayOf(Color.WHITE, Color.BLACK, Color.RED), Rect(0, 250,300, 400))
                                .rotate(-90f)
                            )
                            s1.bitmap?.recycle()
                        }
                    } else null

                    val task2 = if (s2 != lastS2 && s2 != null && s2.bitmap != null) async {
                        lastS2 = s2
                        inky2.setImage(s2.bitmap!!.rotate(90f))
                        s2.bitmap?.recycle()
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
}

data class DisplayQueue(
    val screen1: Screen1?,
    val screen2: Screen2?
)