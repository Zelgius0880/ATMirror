package zelgius.com.atmirror

import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback
import zelgius.com.atmirror.viewModels.MainViewModel
import zelgius.com.atmirror.shared.SharedMainActivity
import zelgius.com.utils.toHexString
import java.io.IOException


private val TAG = MainActivity::class.java.simpleName
private val UART_DEVICE_NAME: String = "UART0"

class MainActivity : SharedMainActivity() {
    override val viewModel by lazy { zelgius.com.utils.ViewModelHelper.create<MainViewModel>(this) }

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
    fun readUartBuffer(uart: UartDevice) {
        // Maximum amount of data to read at one time
        val maxCount = 256

        uart.apply {
            ByteArray(maxCount).also { buffer ->
                var count: Int = read(buffer, buffer.size)
                while (count > 0) {
                    Log.d(TAG, "Read $count bytes from peripheral")
                    //Log.d(TAG, buffer.toHexString())
                    Log.d(TAG, String(buffer))
                    count = read(buffer, buffer.size)

                }

                Log.d(TAG, String(buffer))

            }
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

    private val uartCallback = object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Read available data from the UART device
            try {
                readUartBuffer(uart)
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
