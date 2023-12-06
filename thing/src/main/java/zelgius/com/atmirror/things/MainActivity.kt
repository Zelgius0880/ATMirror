package zelgius.com.atmirror.things

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.utils.Utils
import com.google.android.things.pio.*
import com.test.buzzer.Mario
import com.zelgius.bitmap_ktx.scale
import com.zelgius.bitmap_ktx.toBitmap
import com.zelgius.livedataextensions.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.shared.repository.MeasureType
import zelgius.com.atmirror.shared.viewModel.MirrorNetworkViewModel
import zelgius.com.atmirror.things.compose.Screen1
import zelgius.com.atmirror.things.compose.Screen1View
import zelgius.com.atmirror.things.compose.Screen2
import zelgius.com.atmirror.things.compose.Screen2View
import zelgius.com.atmirror.things.drivers.buzzer.Buzzer
import zelgius.com.atmirror.things.drivers.buzzer.BuzzerAndroidThings
import zelgius.com.atmirror.things.entities.SensorRecord
import zelgius.com.atmirror.things.entities.json.ForecastData
import zelgius.com.atmirror.things.entities.json.OpenWeatherMap
import zelgius.com.atmirror.things.viewModels.InkyViewModel
import zelgius.com.atmirror.things.viewModels.MainViewModel
import zelgius.com.utils.round
import zelgius.com.utils.toHexString
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


private val TAG = MainActivity::class.java.simpleName
private const val UART_DEVICE_NAME: String = "UART0"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val inkyViewModel: InkyViewModel by viewModels()

    private lateinit var networkViewModel: MirrorNetworkViewModel

    lateinit var pwm: Pwm
    val buzzer: Buzzer by lazy { BuzzerAndroidThings(pwm) }

    private var s1: Screen1 = Screen1(null, null, null, null, listOf())
    private var s2: Screen2? = null

    private var mDevice: UartDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Utils.init(LocalContext.current)
            var temperatureInternal by remember { mutableStateOf<Float?>(null) }
            var temperatureExternal by remember { mutableStateOf<Float?>(null) }
            var pressure by remember { mutableStateOf<Int?>(null) }
            var humidity by remember { mutableStateOf<Int?>(null) }
            var history by remember { mutableStateOf<List<SensorRecord>>(listOf()) }

            var forecast by remember { mutableStateOf<List<ForecastData>>(emptyList()) }

            Row(modifier = Modifier.size(600.dp, 400.dp)) {
                Screen1View(
                    history = history,
                    temperature = temperatureInternal,
                    temperatureExternal = temperatureExternal,
                    humidity = humidity,
                    pressure = pressure
                )
                Screen2View(forecast)
            }

            viewModel.currentInsideRecord.observe(this) {
                temperatureInternal = it.temperature.toFloat()
                pressure = it.pressure.toInt()
                humidity = it.humidity.toInt()

                s1 = s1.copy(
                    temperature = temperatureInternal,
                    pressure = pressure,
                    humidity = humidity,
                )

                updateScreen1()
            }

            viewModel.history.observe(this) {
                history = it
                s1 = s1.copy(history = it)
                updateScreen1()
            }

            viewModel.outsideLiveData.observe(this) {
                temperatureExternal = it[MeasureType.Temperature]
                    ?.maxByOrNull { it.time }
                    ?.data
                    ?.toFloat()

                updateScreen1()
            }

            viewModel.forecastLiveData.observe(lifecycleOwner = this) {
                forecast = it

                s2 = Screen2(forecast)
                rootView.postDelayed({
                    bitmap = generateBitmap()
                    s2?.bitmap = screen2
                    inkyViewModel.display(
                        screen1 = s1,
                        screen2 = s2
                    )
                }, 500L)
            }
        }

        mDevice = try {
            PeripheralManager.getInstance()
                .openUartDevice(UART_DEVICE_NAME)
        } catch (e: IOException) {
            Log.w(TAG, "Unable to access UART device", e)
            null
        }

        mDevice?.let {
            configureUartFrame(it)
        }

        networkViewModel = MirrorNetworkViewModel(application)
        pwm = PeripheralManager.getInstance().openPwm("PWM1")
    }

    private fun updateScreen1() {
        rootView.postDelayed({
            bitmap = generateBitmap()
            inkyViewModel.display(
                screen1 = s1.apply { bitmap = screen1 },
                screen2 = s2.apply { bitmap = screen2 }
            )
        }, 500L)
    }

    private lateinit var bitmap: Bitmap

    private val screen1
        get() =
            Bitmap.createBitmap(bitmap, 0, 0, 300, 400)


    private val screen2
        get() =
            Bitmap.createBitmap(bitmap, 300, 0, 300, 400)


    private val rootView by lazy { window.decorView.rootView }

    private fun generateBitmap(): Bitmap {
        if (this::bitmap.isInitialized) bitmap.recycle()
        val (width, height) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            600f,
            resources.displayMetrics
        ).roundToInt() to TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            400f,
            resources.displayMetrics
        ).roundToInt()
        return rootView
            .toBitmap(width, height, Rect(0, 0, width, height)).let {
                it.scale(600, 400).apply {
                    it.recycle()
                }
            }
    }

    fun configureUartFrame(uart: UartDevice) {
        uart.apply {
            // Configure the UART port
            setBaudrate(115200)
            setDataSize(8)
            setParity(UartDevice.PARITY_NONE)
            setStopBits(1)
        }
    }

    fun readUartBuffer(uart: UartDevice) =
        // Maximum amount of data to read at one time
        uart.run {
            val maxCount = 256
            val result = ByteBuffer.allocate(256)
            ByteArray(maxCount).let { buffer ->
                var count: Int = read(buffer, buffer.size)
                while (count > 0) {
                    result.put(buffer, 0, count)
                    Log.d(TAG, "Read $count bytes from peripheral")
                    //Log.d(TAG, buffer.toHexString())
                    //Log.d(TAG, String(buffer))
                    count = read(buffer, buffer.size)

                }

                Log.d(TAG, result.array().toHexString())
                result.array().slice(0 until result.position()).toByteArray()
            }

        }


    override fun onStart() {
        super.onStart()
        // Begin listening for interrupt events
        mDevice?.registerUartDeviceCallback(uartCallback)

        lifecycleScope.launch {
            delay(1000)
            buzzer.playMelody(Mario.melodyShort.first, Mario.melodyShort.second)
        }

    }

    override fun onStop() {
        super.onStop()
        // Interrupt events no longer necessary
        mDevice?.unregisterUartDeviceCallback(uartCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        pwm.close()
        playing = false
    }

    private var playing = false

    private var lastSignal = 0L
    private val uartCallback = object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Read available data from the UART device
            try {
                val (melody, tempo) = Mario.fireBall

                thread {
                    if (!playing) {
                        playing = true
                        runBlocking {
                            buzzer.playMelody(melody, tempo)
                        }
                        playing = false
                    }
                }

                val bytes = readUartBuffer(uart)


                if((Date().time - lastSignal).milliseconds > 2.seconds) {
                    networkViewModel.switchPressed(bytes)
                        .observe(lifecycleOwner = this@MainActivity) {
                        }

                    lastSignal = Date().time
                }
            } catch (e: IOException) {
                Log.w(TAG, "Unable to access UART device", e)
            }

            // Continue listening for more interrupts
            return true
        }

        override fun onUartDeviceError(uart: UartDevice?, error: Int) {
            Log.w(TAG, "$uart: Error event $error")
        }
    }
}

fun PeripheralManager.openOutput(name: String): Gpio =
    openGpio(name).apply {
        setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }


fun PeripheralManager.openIutput(name: String): Gpio =
    openGpio(name).apply {
        setDirection(Gpio.DIRECTION_IN)
    }
