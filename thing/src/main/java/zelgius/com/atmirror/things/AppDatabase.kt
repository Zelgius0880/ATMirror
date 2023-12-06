package zelgius.com.atmirror.things

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import zelgius.com.atmirror.things.dao.SensorRecordDAO
import zelgius.com.atmirror.things.entities.SensorRecord


@Database(entities = [SensorRecord::class], version = 3)
abstract class AppDatabase: RoomDatabase() {
    abstract val sensorRecordDao: SensorRecordDAO

    companion object {
        var db: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS UnknownSignal (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `hexa` TEXT NOT NULL, `length` INTEGER NOT NULL, `raw` BLOB NOT NULL)")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS UnknownSignal")
                database.execSQL("CREATE TABLE IF NOT EXISTS UnknownSignal (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `hexa` TEXT NOT NULL, `date` TEXT NOT NULL, `length` INTEGER NOT NULL, `raw` BLOB NOT NULL)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            if(db == null) db =Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

            return db!!
        }
    }
}