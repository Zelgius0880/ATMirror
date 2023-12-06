package zelgius.com.atmirror.things.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.shared.repository.MeasureType
import zelgius.com.atmirror.shared.repository.NetatmoRepository
import zelgius.com.atmirror.things.proto.OpenWeatherMapProto
import zelgius.com.atmirror.things.protobuf.NetatmoResultProto
import zelgius.com.atmirror.things.protobuf.toProto
import zelgius.com.atmirror.things.repositories.OpenWeatherMapRepository
import java.time.LocalDateTime
import javax.inject.Named

@HiltWorker
class NetatmoWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: NetatmoRepository,
    @Named("Outside") private val outsideDataStore: DataStore<NetatmoResultProto>,
    @Named("Inside") private val insideDataStore: DataStore<NetatmoResultProto>
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return runBlocking {
            val inside = inputData.getBoolean("inside", false)

            with(
                if (inside)
                    repository.getMeasure(
                        false,
                        dateBegin = LocalDateTime.now().minusDays(1),
                        measure = arrayOf(
                            MeasureType.Temperature,
                            MeasureType.Pressure,
                            MeasureType.Humidity
                        )
                    )
                else
                    repository.getMeasure(true)
            ) {
                return@with if (this.isEmpty()) Result.retry() else {
                    (if (inside) insideDataStore
                    else outsideDataStore)
                        .updateData {
                            this.toProto()
                        }
                    Result.success()
                }
            }
        }
    }
}
