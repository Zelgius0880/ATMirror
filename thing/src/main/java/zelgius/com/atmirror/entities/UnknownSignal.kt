package zelgius.com.atmirror.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
data class UnknownSignal(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val hexa: String,
    val length: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val raw: ByteArray,
    val date: String
) {

    constructor(
        raw: ByteArray,
        date: LocalDateTime = LocalDateTime.now(),
        hexa: String,
        length: Int,
        id: Long? = null
    ) : this(
        id, hexa, length, raw, date.format(
            DateTimeFormatter.ISO_DATE_TIME
        )
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownSignal

        if (hexa != other.hexa) return false
        if (length != other.length) return false
        if (!raw.contentEquals(other.raw)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hexa.hashCode()
        result = 31 * result + length
        result = 31 * result + raw.contentHashCode()
        return result
    }
}