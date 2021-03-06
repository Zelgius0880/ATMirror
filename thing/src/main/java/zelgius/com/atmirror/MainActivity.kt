package zelgius.com.atmirror

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.facebook.stetho.Stetho
import com.github.mikephil.charting.utils.Utils
import com.google.android.things.pio.*
import com.test.buzzer.Mario
import com.zelgius.bitmap_ktx.scale
import com.zelgius.bitmap_ktx.toBitmap
import com.zelgius.livedataextensions.observe
import khronos.Dates
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.compose.Screen1
import zelgius.com.atmirror.compose.Screen1View
import zelgius.com.atmirror.compose.Screen2
import zelgius.com.atmirror.compose.Screen2View
import zelgius.com.atmirror.drivers.buzzer.Buzzer
import zelgius.com.atmirror.drivers.buzzer.BuzzerAndroidThings
import zelgius.com.atmirror.entities.SensorRecord
import zelgius.com.atmirror.entities.UnknownSignal
import zelgius.com.atmirror.entities.json.City
import zelgius.com.atmirror.entities.json.OpenWeatherMap
import zelgius.com.atmirror.shared.viewModel.MirrorNetworkViewModel
import zelgius.com.atmirror.worker.DarkSkyResult
import zelgius.com.atmirror.viewModels.InkyViewModel
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.worker.NetatmoResult
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.round
import zelgius.com.utils.toHexString
import java.io.IOException
import java.lang.Exception
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


    private var s1: Screen1 = Screen1(null, null, null, null, listOf())
    private var s2: Screen2? = null

    private var lastUpdate: Long = 0
    private var mDevice: UartDevice? = null

    private val pwm0: Pwm by lazy {
        PeripheralManager.getInstance().openPwm("PWM0").apply {
            setPwmDutyCycle(0.0)
            setPwmFrequencyHz(256.0)
            setEnabled(true)
        }
    }

    private var currentPwm0: Pwm? = null

    private var weatherMap: OpenWeatherMap? = null

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            weatherMap?.let {
                if (currentPwm0 == null
                    && System.currentTimeMillis() / 1000 > it.list.first().sunset
                    && System.currentTimeMillis() / 1000 < it.list[1].sunrise
                )

                    lifecycleScope.launch {
                        (0..100).forEach { i ->
                            pwm0.setPwmDutyCycle(i.toDouble())
                            delay(10)
                        }

                        currentPwm0 = pwm0
                    }
                else {
                    lifecycleScope.launch {
                        (100 downTo 0).forEach { i ->
                            pwm0.setPwmDutyCycle(i.toDouble())
                            delay(10)
                        }
                        currentPwm0 = null
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(this)


        setContent {
            Utils.init(ContextAmbient.current)
            val stateTemperature: MutableState<Float?> = state { null }
            val statePressure: MutableState<Int?> = state { null }
            val stateHumidity: MutableState<Int?> = state { null }
            val stateHistory: MutableState<List<SensorRecord>> = state { listOf<SensorRecord>() }
            val stateForecast: MutableState<OpenWeatherMap> =
                state { OpenWeatherMap(City(name = "", country = "")) }
            val stateExternalTemperature: MutableState<Float?> = state { null }
            //FIXME using remember{} instead of state{}

            Row(modifier = Modifier.size(600.dp, 400.dp)) {
                Screen1View(
                    history = stateHistory,
                    temperature = stateTemperature,
                    temperatureExternal = stateExternalTemperature,
                    humidity = stateHumidity,
                    pressure = statePressure
                )
                Screen2View(stateForecast)
            }

            viewModel.sht21Record.observerAndUpdateScreen1(stateTemperature) {
                if (it.temperature.toFloat().round(1) == stateTemperature.value?.round(1))
                    null
                else {
                    s1 = s1.copy(temperature = it.temperature.toFloat())
                    it.temperature.toFloat().round(1)
                }
            }

            viewModel.sht21Record.observerAndUpdateScreen1(stateHumidity) {
                if (it.humidity.roundToInt() == stateTemperature.value?.roundToInt())
                    null
                else {
                    s1 = s1.copy(humidity = it.humidity.roundToInt())
                    it.humidity.roundToInt()
                }
            }

            viewModel.history.observerAndUpdateScreen1(stateHistory) {
                val list = it.subList((it.size - 24).coerceAtLeast(0), it.size)
                if (stateHistory.value.containsAll(list)) null
                else {
                    s1 = s1.copy(history = list)
                    it
                }
            }

            viewModel.piclockCurrentRecord.observerAndUpdateScreen1(statePressure) {
                viewModel.updateLastKnownRecord(it)

                if (it.pressure.roundToInt() == statePressure.value)
                    null
                else {
                    s1 = s1.copy(pressure = it.pressure.roundToInt())
                    it.pressure.roundToInt()
                }
            }

            viewModel.netatmoLiveData.observerAndUpdateScreen1(stateExternalTemperature) {
                val currentTemperature = it.maxByOrNull { pair -> pair.first }?.second
                if (currentTemperature == null || currentTemperature.toFloat()
                        .round(1) == stateTemperature.value?.round(1)
                )
                    null
                else {
                    s1 = s1.copy(temperatureExternal = currentTemperature.toFloat())
                    currentTemperature.toFloat().round(1)
                }
            }

            viewModel.forecastLiveData.observe(lifecycleOwner = this) {
                stateForecast.value = it
                weatherMap = it

                s2 = Screen2(it)
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

        networkViewModel = ViewModelHelper.create(this)

        pwm = PeripheralManager.getInstance().openPwm("PWM1")

        viewModel.getRecordHistory(from = Dates.yesterday)

        viewModel.workerOwmStatus.observe(lifecycleOwner = this) { list ->
            list.forEach {

                when (it.state) {
                    //ENQUEUED because a PeriodicWork never goes SUCCEEDED, it goes directly to ENQUEUED
                    WorkInfo.State.SUCCEEDED, WorkInfo.State.ENQUEUED -> {

                        DarkSkyResult.result?.apply {
                            viewModel.forecastLiveData.value = this
                        }
                    }
                    else -> {
                    }
                }
            }
        }

        viewModel.workerNetatmoStatus.observe(lifecycleOwner = this) { list ->
            list.forEach {

                when (it.state) {
                    //ENQUEUED because a PeriodicWork never goes SUCCEEDED, it goes directly to ENQUEUED
                    WorkInfo.State.SUCCEEDED, WorkInfo.State.ENQUEUED -> {

                        NetatmoResult.result?.apply {
                            viewModel.netatmoLiveData.value = this
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun <T, S> LiveData<T>.observerAndUpdateScreen1(
        state: MutableState<S>,
        update: (T) -> S?
    ) {
        observe(context) {
            with(update(it)) {
                if (this != null/* && currentTimeStamp - lastUpdate >= 60 * 1000*/) {
                    state.value = this
                    rootView.postDelayed({
                        bitmap = generateBitmap()
                        inkyViewModel.display(
                            screen1 = s1.apply { bitmap = screen1 },
                            screen2 = s2.apply { bitmap = screen2 }
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

        try {
            registerReceiver(
                tickReceiver,
                IntentFilter().apply { addAction(Intent.ACTION_TIME_TICK) })
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        super.onStop()
        // Interrupt events no longer necessary
        mDevice?.unregisterUartDeviceCallback(uartCallback)

        try {
            unregisterReceiver(tickReceiver)
        } catch (_: Exception) {
        }
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

                val bytes = readUartBuffer(uart)
                networkViewModel.switchPressed(bytes).observe(lifecycleOwner = this@MainActivity) {
                    if(!it) viewModel.saveUnknownSignal(
                        UnknownSignal(
                            hexa = bytes.toHexString(),
                            length = bytes.size,
                            raw = bytes
                        )
                    )
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
