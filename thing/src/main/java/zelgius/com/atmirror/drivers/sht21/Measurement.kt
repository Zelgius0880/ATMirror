package zelgius.com.atmirror.drivers.sht21

import java.util.*

/**
 * A `Measurement` consists of
 *
 *  * The [MeasureType]
 *  * The value
 *  * The creation [Date]
 *
 */
class Measurement private constructor(
    /**
     * Return the measured value.
     *
     * @return measured value
     */
    val value: Float,
    /**
     * Return the [MeasureType].
     *
     * @return The [MeasureType].
     */
    val type: MeasureType, private val createdAt: Date = Date()
) {

    /**
     * Return the creation [Date].
     *
     * @return The creation [Date].
     */
    fun getCreatedAt(): Date {
        return Date(this.createdAt.time)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as Measurement?

        if (that!!.value.compareTo(value) != 0) {
            return false
        }
        return if (createdAt != that.createdAt) {
            false
        } else type === that.type

    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + if (value != +0.0f) java.lang.Float.floatToIntBits(value) else 0
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "Measurement{" +
                "createdAt=" + createdAt +
                ", value=" + value +
                ", type=" + type +
                '}'.toString()
    }

    /**
     * Return the json representation of this [Measurement].
     *
     * @return `String` containing the json representation of this [Measurement].
     */
    /*fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }*/

    companion object {


        /**
         * Create a new `Measurement`.
         *
         * @param value measured value.
         * @param type  [MeasureType].
         * @return A new `Measurement`.
         */
        fun create(value: Float, type: MeasureType): Measurement {
            Objects.requireNonNull(type, MeasureType::class.java.canonicalName!! + " is null.")
            return Measurement(value, type)
        }

        /**
         * Create a new `Measurement`.
         *
         * @param value measured value.
         * @param type  [MeasureType].
         * @return A new `Measurement`.
         */
        fun create(value: Float, type: MeasureType, measuredAt: Date): Measurement {
            return Measurement(value, type, measuredAt)
        }
    }
}
/**
 * Create a new `Measurement`.
 *
 * @param value measured value.
 * @param type  [MeasureType].
 */
