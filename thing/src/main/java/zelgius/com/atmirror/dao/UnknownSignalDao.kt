package zelgius.com.atmirror.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import zelgius.com.atmirror.entities.UnknownSignal

@Dao
interface UnknownSignalDao {
    @Insert
    suspend fun insert(item: UnknownSignal): Long

    @Update
    suspend fun update(item: UnknownSignal): Int

    @Delete
    suspend fun delete(item: UnknownSignal): Int
}