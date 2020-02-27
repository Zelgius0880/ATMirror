package zelgius.com.utils

import android.content.Context
import java.time.ZoneId
import java.util.*
import kotlin.math.round
import android.graphics.Color
import androidx.annotation.ColorRes
import kotlin.experimental.and
import kotlin.math.roundToInt


fun Date.toLocalDateTime() =
    this.toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime()!!

fun Date.toLocalDateTime(timeZone: ZoneId) =
    this.toInstant().atZone(timeZone)
        .toLocalDateTime()!!

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun Context.getColor(@ColorRes color: Int, alpha: Float) =
    getColor(color).let{
        Color.argb(
            (Color.alpha(color) * alpha).roundToInt(),
            Color.red(it),
            Color.green(it),
            Color.blue(it))
    }


fun ByteArray.toHexString(): String {
    val hexArray = "0123456789ABCDEF".toCharArray()
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v = (this[j] and 0xFF.toByte()).toInt()

        hexChars[j * 2] = hexArray[v ushr 4]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}


fun String.hexStringToByteArray() : ByteArray {
    val HEX_CHARS = "0123456789ABCDEF"
    val result = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS.indexOf(this[i]);
        val secondIndex = HEX_CHARS.indexOf(this[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}

