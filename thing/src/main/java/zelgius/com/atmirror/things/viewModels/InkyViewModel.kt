package zelgius.com.atmirror.things.viewModels

import android.app.Application
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.zelgius.bitmap_ktx.floydSteinbergDithering
import com.zelgius.bitmap_ktx.rotate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import zelgius.com.atmirror.things.compose.Screen1
import zelgius.com.atmirror.things.compose.Screen2
import zelgius.com.atmirror.things.drivers.inky.WHatHALThing
import zelgius.com.atmirror.things.openIutput
import zelgius.com.atmirror.things.openOutput
import java.time.LocalDateTime


val TAG = InkyViewModel::class.simpleName

@SuppressWarnings("")
class InkyViewModel(private val app: Application) : AndroidViewModel(app) {

    private val dc: Gpio = PeripheralManager.getInstance().openOutput("BCM22")
    private val queue = MutableStateFlow(DisplayQueue(null, null))

    private val inky2 by lazy {
        with(PeripheralManager.getInstance()) {
            WHatHALThing(
                dc = dc,
                reset = openOutput("BCM16"),
                busy = openIutput("BCM5"),
                spi = spi2
            )
        }
    }

    private val inky1 by lazy {
        with(PeripheralManager.getInstance()) {
            WHatHALThing(
                dc = dc,
                reset = openOutput("BCM19"),
                busy = openIutput("BCM6"),
                spi = spi1
            )
        }
    }


    private val spi1 by lazy {
        val manager = PeripheralManager.getInstance()
        manager.openSpiDevice("SPI0.0")
    }
    private val spi2 by lazy {
        val manager = PeripheralManager.getInstance()
        manager.openSpiDevice("SPI0.1")
    }

    private var lastS1: Screen1? = null
    private var lastS2: Screen2? = null

    private var stop = false

    init {
        viewModelScope.launch {
            queue.collect {
                val (s1, s2) = it

                val task1 = if (s1 != lastS1 && s1 != null && s1.bitmap != null) {
                    async {
                        Log.e(TAG, "${LocalDateTime.now()} Computing screen 1")
                        inky1.setImage(
                            s1.bitmap!!
                                .floydSteinbergDithering(
                                    intArrayOf(
                                        Color.WHITE,
                                        Color.BLACK,
                                        Color.RED
                                    ), Rect(0, 0, 300, 200)
                                )
                                .floydSteinbergDithering(
                                    intArrayOf(
                                        Color.WHITE,
                                        Color.BLACK,
                                        Color.RED
                                    ), Rect(0, 250, 300, 400)
                                ).let {
                                    it.rotate(-90f).apply { it.recycle() }
                                }

                        )
                        s1.bitmap?.recycle()
                        lastS1 = s1
                    }
                } else null

                val task2 = if (s2 != lastS2 && s2 != null && s2.bitmap != null) async {
                    Log.e(TAG, "${LocalDateTime.now()} Computing screen 2")
                    inky2.setImage(s2.bitmap!!.let {
                        it.rotate(90f).apply { it.recycle() }
                    })
                    s2.bitmap?.recycle()
                    lastS2 = s2
                } else null

                Log.e(TAG, "$task1 $task2 ")
                task2?.await()
                if (task2 != null) inky2.show(true)

                task1?.await()
                if (task1 != null) inky1.show(true)

                Log.e(TAG, " --- Done --- ")
            }
        }
    }

    fun display(screen1: Screen1? = null, screen2: Screen2? = null) {
        viewModelScope.launch {
            queue.emit(DisplayQueue(screen1 ?: lastS1, screen2 ?: lastS2))
        }
    }


    override fun onCleared() {
        super.onCleared()
        inky1.close()
        inky2.close()
        stop = true
    }
}

data class DisplayQueue(
    val screen1: Screen1?,
    val screen2: Screen2?
)