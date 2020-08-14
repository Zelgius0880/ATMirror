package zelgius.com.atmirror.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import zelgius.com.atmirror.BuildConfig
import zelgius.com.atmirror.entities.json.OpenWeatherMap
import zelgius.com.atmirror.repositories.OpenWeatherMapRepository

const val KEY = BuildConfig.DARKSKY_KEY
const val LATITUDE = BuildConfig.LATITUDE
const val LONGITUDE = BuildConfig.LONGITUDE

object DarkSkyResult {
    var result: OpenWeatherMap? = null
}

class DarkSkyWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {

        with(OpenWeatherMapRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).execute()) {
            if (!isSuccessful) return Result.retry()

            val result = body() ?: return Result.failure()
            DarkSkyResult.result = result

            return Result.success()
        }
    }


}