package zelgius.com.atmirror.shared

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import zelgius.com.atmirror.shared.viewModels.SharedMainViewModel


private val TAG = SharedMainActivity::class.java.simpleName

abstract class SharedMainActivity : AppCompatActivity() {
    /*private lateinit var mSensorManager: SensorManager
    private val mDynamicSensorCallback = object : DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
            if (sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Log.i(TAG, "Temperature sensor connected")
                mSensorEventListener = TemperaturePressureEventListener()
                mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }
    private lateinit var mSensorEventListener: TemperaturePressureEventListener
*/

    protected abstract val viewModel : SharedMainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main)
        //startTemperaturePressureRequest()
        ViewModelProviders.of(this).get<SharedMainViewModel>()
        val decorView = window.decorView
// Hide the status bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        decorView.systemUiVisibility = uiOptions
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.



        val brightness = 100 / 255f
        val lp = window.attributes
        lp.screenBrightness = 0.1f
        window.attributes = lp
    }

    override fun onDestroy() {
        super.onDestroy()
        //stopTemperaturePressureRequest()
    }

    /* private fun startTemperaturePressureRequest() {
         this.startService(Intent(this, TemperaturePressureService::class.java))
         mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
         mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback)
     }

     private fun stopTemperaturePressureRequest() {
         this.stopService(Intent(this, TemperaturePressureService::class.java))
         mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback)
         mSensorManager.unregisterListener(mSensorEventListener)
     }

     private inner class TemperaturePressureEventListener : SensorEventListener {
         override fun onSensorChanged(event: SensorEvent) {
             Log.i(TAG, "sensor changed: " + event.values[0])
         }

         override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
             Log.i(TAG, "sensor accuracy changed: " + accuracy)
         }
     }*/


    override fun onResume() {
        super.onResume()
        viewModel.registerDrivers()
    }

    override fun onPause() {
        super.onPause()

        viewModel.unregisterDrivers()
    }


    companion object {
        //private SensorManager mSensorManager;

    }
}
