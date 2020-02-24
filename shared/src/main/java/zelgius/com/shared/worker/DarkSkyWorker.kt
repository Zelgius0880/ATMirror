package zelgius.com.shared.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import zelgius.com.shared.BuildConfig
import zelgius.com.shared.R
import zelgius.com.shared.entities.json.DarkSky
import zelgius.com.shared.repositories.DarkSkyRepository

const val KEY = BuildConfig.DARKSKY_KEY
const val LATITUDE = BuildConfig.LATITUDE
const val LONGITUDE = BuildConfig.LONGITUDE

object DarkSkyResult {
    var result: DarkSky? = null
}

class DarkSkyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {

        with(DarkSkyRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).execute()) {
            if (!isSuccessful) return Result.retry()

            val result = body() ?: return Result.failure()
            DarkSkyResult.result = result

            return Result.success()
        }
    }


}