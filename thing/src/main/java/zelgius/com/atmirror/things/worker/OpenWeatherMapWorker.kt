package zelgius.com.atmirror.things.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import zelgius.com.atmirror.things.BuildConfig
import zelgius.com.atmirror.things.entities.json.OpenWeatherMap
import zelgius.com.atmirror.things.proto.ForecastProto
import zelgius.com.atmirror.things.proto.OpenWeatherMapProto
import zelgius.com.atmirror.things.protobuf.toProto
import zelgius.com.atmirror.things.repositories.OpenWeatherMapRepository
import zelgius.com.atmirror.things.utils.putParcelable
import zelgius.com.atmirror.things.utils.putParcelableList

const val KEY = BuildConfig.DARKSKY_KEY
const val LATITUDE = BuildConfig.LATITUDE
const val LONGITUDE = BuildConfig.LONGITUDE


@HiltWorker
class OpenWeatherMapWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val openWeatherMapRepository: OpenWeatherMapRepository,
    private val resultDataStore: DataStore<OpenWeatherMapProto>
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

        with(openWeatherMapRepository.service.getForecast(KEY, LATITUDE, LONGITUDE).execute()) {
            if (!isSuccessful) return Result.retry()

            val result = body() ?: return Result.failure()

            resultDataStore.updateData {
                result.toProto()
            }

            return Result.success()
        }
    }


}