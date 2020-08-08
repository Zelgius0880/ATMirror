package zelgius.com.atmirror.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import java.util.*

@Entity
class SensorRecord {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @get:PropertyName("temp") @set:PropertyName("temp")
    var temperature = 0.0
        set(value) {
            field = value
            temperaturePresent = true
        }

    var humidity = 0.0
        set(value) {
            field = value
            humidityPresent = true
        }

    var pressure = 0.0
        set(value) {
            field = value
            pressurePresent = true
        }

    @get:PropertyName("elevation") @set:PropertyName("elevation")
    var altitude = 0.0
        set(value) {
            field = value
            altitudePresent = true
        }

    var temperaturePresent = false
    var humidityPresent = false
    var pressurePresent = false
    var altitudePresent = false

    var stamp: Long = 0L

    val date
        get() = Date(stamp)

    constructor()


    @Ignore
    constructor(temperature: Double?, humidity: Double?, pressure: Double?, altitude: Double?) {
        this.temperature = temperature ?: 0.0
        temperaturePresent = temperature != null

        this.humidity = humidity ?: 0.0
        humidityPresent = humidity != null

        this.pressure = pressure ?: 0.0
        pressurePresent = pressure != null

        this.altitude = altitude ?: 0.0
        altitudePresent = altitude != null
    }

    override fun toString(): String {
        return "SensorRecord(temperature=$temperature, humidity=$humidity, pressure=$pressure, altitude=$altitude, temperaturePresent=$temperaturePresent, humidityPresent=$humidityPresent, pressurePresent=$pressurePresent, altitudePresent=$altitudePresent, stamp=$stamp, date=$date)"
    }


}