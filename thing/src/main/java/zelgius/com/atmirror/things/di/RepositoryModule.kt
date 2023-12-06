package zelgius.com.atmirror.things.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zelgius.com.atmirror.shared.repository.NetatmoRepository
import zelgius.com.atmirror.things.proto.ForecastProto
import zelgius.com.atmirror.things.proto.OpenWeatherMapProto
import java.io.File
import javax.inject.Singleton
import zelgius.com.atmirror.things.protobuf.*
import zelgius.com.atmirror.things.repositories.DatabaseRepository
import zelgius.com.atmirror.things.repositories.OpenWeatherMapRepository
import javax.inject.Named

/**
 * In order to work with data store easily without having the multi instance, the DI is a best way
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun forecastWorkResultDataStore(@ApplicationContext context: Context): DataStore<OpenWeatherMapProto> =
        MultiProcessDataStoreFactory.create(
            serializer = ForecastSerializer,
            produceFile = {
                File("${context.cacheDir.path}/forecast_results.preferences_pb")
            }
        )

    @Provides
    @Singleton
    @Named("Outside")
    fun outsideWorkResultDataStore(@ApplicationContext context: Context): DataStore<NetatmoResultProto> =
        MultiProcessDataStoreFactory.create(
            serializer = NetatmoResultSerializer,
            produceFile = {
                File("${context.cacheDir.path}/outside_results.preferences_pb")
            }
        )

    @Provides
    @Singleton
    @Named("Inside")
    fun insideWorkResultDataStore(@ApplicationContext context: Context): DataStore<NetatmoResultProto> =
        MultiProcessDataStoreFactory.create(
            serializer = NetatmoResultSerializer,
            produceFile = {
                File("${context.cacheDir.path}/inside_results.preferences_pb")
            }
        )

    @Provides
    @Singleton
    fun openWeatherMapRepository() = OpenWeatherMapRepository()

    @Provides
    @Singleton
    fun netatmoRepository() = NetatmoRepository()

    @Provides
    @Singleton
    fun databaseRepository(@ApplicationContext context: Context) = DatabaseRepository(context)

    @Provides
    @Singleton
    fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
}