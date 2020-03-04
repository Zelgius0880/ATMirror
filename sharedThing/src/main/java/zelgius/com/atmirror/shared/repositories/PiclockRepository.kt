package zelgius.com.atmirror.shared.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import zelgius.com.atmirror.shared.entities.SensorRecord
import java.util.*

val TAG: String = PiclockRepository::class.java.simpleName

class PiclockRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("TempData")

    private val currentRecord = MutableLiveData<SensorRecord>()

    private val currentRecordListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val record = dataSnapshot.getValue(SensorRecord::class.java)

            currentRecord.postValue(record)
        }

    }

    fun listenCurrentRecord(): LiveData<SensorRecord> {
        ref.child("current").child("data").addValueEventListener(currentRecordListener)

        return currentRecord
    }

    fun unlistenCurrentRecord() {
        ref.child("current").child("data").removeEventListener(currentRecordListener)
    }

    fun getSensorDataHistory(from: Long, to: Long = Date().time): LiveData<List<SensorRecord>> {
        val liveData = MutableLiveData<List<SensorRecord>>()
        val list = mutableListOf<SensorRecord>()

        ref.child("data")
            .orderByChild("stamp")
            .startAt(from.toDouble())
            .endAt(to.toDouble()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (sensorSnapshot in dataSnapshot.children) {
                        list.add(sensorSnapshot.getValue(SensorRecord::class.java)!!)
                    }

                    liveData.value = list
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    databaseError.toException().printStackTrace()
                }
            })
        return liveData
    }

    fun getSensorDataHistory(from: Long, to: Long = Date().time, callback: (List<SensorRecord>) -> Unit) {
        val list = mutableListOf<SensorRecord>()

        ref.child("data")
            .orderByChild("stamp")
            .startAt(from.toDouble())
            .endAt(to.toDouble()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (sensorSnapshot in dataSnapshot.children) {
                        list.add(sensorSnapshot.getValue(SensorRecord::class.java)!!)
                    }

                    callback(list)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    databaseError.toException().printStackTrace()
                    callback(listOf())
                }
            })
    }
}