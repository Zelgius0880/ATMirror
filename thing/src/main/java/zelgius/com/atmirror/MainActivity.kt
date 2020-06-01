package zelgius.com.atmirror

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback
import com.test.buzzer.Mario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.drivers.buzzer.Buzzer
import zelgius.com.atmirror.drivers.buzzer.BuzzerAndroidThings
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedMainActivity
import zelgius.com.atmirror.shared.viewModel.MirrorNetworkViewModel
import zelgius.com.utils.ViewModelHelper
import zelgius.com.utils.toHexString
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.concurrent.thread


private val TAG = MainActivity::class.java.simpleName
private val UART_DEVICE_NAME: String = "UART0"

class MainActivity : SharedMainActivity() {
    override val viewModel by lazy { ViewModelHelper.create<MainViewModel>(this) }
    private lateinit var networkViewModel : MirrorNetworkViewModel

    lateinit var pwm: Pwm
    val buzzer: Buzzer by lazy { BuzzerAndroidThings(pwm) }

    private var mDevice: UartDevice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        networkViewModel =  ViewModelHelper.create(this)

        pwm = PeripheralManager.getInstance().openPwm("PWM1")
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
                    if(!playing) {
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
}
