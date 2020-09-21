package zelgius.com.atmirror.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.repositories.NetatmoRepository
import java.time.LocalDateTime

object NetatmoResult {
    var result: List<Pair<LocalDateTime, Double>>? = null
}

class NetatmoWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val repository = NetatmoRepository(false)
    override fun doWork(): Result {
        return runBlocking {
            with(repository.getTemperatureMeasure(true) ){
                return@with if(this == null) Result.retry() else{
                    NetatmoResult.result = this
                    Result.success()
                }
            }
        }
    }
}