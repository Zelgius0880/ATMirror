package zelgius.com.atmirror

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import zelgius.com.atmirror.dao.SensorRecordDAO
import zelgius.com.atmirror.entities.SensorRecord


@Database(entities = [SensorRecord::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract val sensorRecordDao: SensorRecordDAO

    companion object {
        var db: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if(db == null) db =Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database"
            ).build()

            return db!!
        }
    }
}