package zelgius.com.atmirror

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.state
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.facebook.stetho.Stetho
import com.google.android.things.pio.*
import com.test.buzzer.Mario
import khronos.Dates
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.compose.Screen1
import zelgius.com.atmirror.compose.Screen1View
import zelgius.com.atmirror.compose.Screen2
import zelgius.com.atmirror.drivers.buzzer.Buzzer
import zelgius.com.atmirror.drivers.buzzer.BuzzerAndroidThings
import zelgius.com.atmirror.entities.SensorRecord
import zelgius.com.atmirror.entities.json.DarkSky
import zelgius.com.atmirror.shared.viewModel.MirrorNetworkViewModel
import zelgius.com.atmirror.worker.DarkSkyResult
import zelgius.com.atmirror.viewModels.InkyViewModel
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.viewModels.scale
import zelgius.com.atmirror.viewModels.toBitmap
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.round
import zelgius.com.utils.toHexString
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.concurrent.thread
import kotlin.math.roundToInt


private val TAG = MainActivity::class.java.simpleName
private const val UART_DEVICE_NAME: String = "UART0"

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelHelper.create<MainViewModel>(this) }
    private val inkyViewModel by lazy { ViewModelHelper.create<InkyViewModel>(this) }

    private lateinit var networkViewModel: MirrorNetworkViewModel

    lateinit var pwm: Pwm
    val buzzer: Buzzer by lazy { BuzzerAndroidThings(pwm) }

    private val context by lazy { this }


    private var s1: Screen1 = Screen1(null, null)
    private var s2: Screen2 = Screen2(DarkSky())

    private var lastUpdate: Long = 0
    private var mDevice: UartDevice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(this)
        setContent {
            val stateTemperature: MutableState<Float?> = state{null}
            val statePressure: MutableState<Int?> = state{null}
            val stateHistory: MutableState<List<SensorRecord>> = state{ listOf()}

            Row(modifier = Modifier.size(600.dp, 400.dp)) {
                Screen1View(
                    history = stateHistory,
                    temperature = stateTemperature,
                    pressure =  statePressure
                )
                Screen1View(
                    history = stateHistory,
                    temperature = stateTemperature,
                    pressure =  statePressure
                )
            }

            viewModel.sht21Record.observerAndUpdateScreen1(stateTemperature) {
                if(it.temperature.toFloat().round(1) == stateTemperature.value?.round(1))
                    null
                else {
                    s1 = Screen1(s1.pressure, it.temperature.toFloat())
                    it.temperature.toFloat().round(1)
                }
            }

            viewModel.piclockCurrentRecord.observerAndUpdateScreen1(statePressure) {
                viewModel.updateLastKnownRecord(it)

                if(it.pressure.roundToInt() == statePressure.value)
                    null
                else {
                    s1 = Screen1(it.pressure.toFloat(), s1.temperature)
                    it.pressure.roundToInt()
                }
            }

            viewModel.forecastLiveData
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

        networkViewModel = ViewModelHelper.create(this)

        pwm = PeripheralManager.getInstance().openPwm("PWM1")

        viewModel.getRecordHistory(from = Dates.yesterday)

        viewModel.forecastLiveData.observe(this){
        }

        viewModel.workerStatus.observe(this) {list ->
            list.forEach {

                when (it.state) {
                    //ENQUEUED because a PeriodicWork never goes SUCCEEDED, it goes directly to ENQUEUED
                    WorkInfo.State.SUCCEEDED, WorkInfo.State.ENQUEUED -> {

                        DarkSkyResult.result?.apply {
                            viewModel.forecastLiveData.value = this
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun<T, S> LiveData<T>.observerAndUpdateScreen1(state: MutableState<S>, update: (T) -> S?) {
        observe(context) {
            with(update(it) ) {
                if ( this != null && currentTimeStamp - lastUpdate >= 60 * 1000) {
                state.value = this
                rootView.postDelayed({
                    bitmap = generateBitmap()
                    inkyViewModel.display(
                        screen1 = s1.apply { bitmap = screen1 }
                    )
                }, 500L)
            }
            }
        }
    }

    private val currentTimeStamp
    get() = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private lateinit var bitmap: Bitmap

    private val screen1
        get() =
            Bitmap.createBitmap(bitmap, 0, 0, 300, 400)


    private val screen2
        get() =
            Bitmap.createBitmap(bitmap, 300, 0, 300, 400)


    private val rootView by lazy { window.decorView.rootView }

    private fun generateBitmap(): Bitmap {
        val (width, height ) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            600f,
            resources.displayMetrics
        ).roundToInt() to TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            400f,
            resources.displayMetrics
        ).roundToInt()
        return rootView
            .toBitmap(width,height, Rect(0, 0, width, height))
            .scale(600, 400)
    }

    @Throws(IOException::class)
    fun configureUartFrame(uart: UartDevice) {
        uart.apply {
            // Configure the UART port
            setBaudrate(115200)
            setDataSize(8)
            setParity(UartDevice.PARITY_NONE)
            setStopBits(1)
        }
    }

    @Throws(IOException::class)
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

                networkViewModel.switchPressed(readUartBuffer(uart))
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

    override fun onResume() {
        super.onResume()
        viewModel.registerDrivers()
    }

    override fun onPause() {
        super.onPause()

        viewModel.unregisterDrivers()
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
