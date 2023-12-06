package zelgius.com.atmirror.things.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.things.device.DeviceManager

class RebootWorker (appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams)  {
    override fun doWork(): Result {
        DeviceManager.getInstance().reboot()

        return Result.success()
    }

}