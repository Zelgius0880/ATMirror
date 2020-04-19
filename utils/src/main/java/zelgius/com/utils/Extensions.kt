package zelgius.com.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

fun Long.toLocalDateTime() =
    LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        TimeZone.getDefault().toZoneId());

fun Long.toLocalDateTime(zone: ZoneId) =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this),zone);

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
    val sb = StringBuilder(size * 2)
    forEach { sb.append(String.format("%02x", it)) }
    return sb.toString()
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

/**
 *
 * Get the value of dp to Pixel according to density of the screen
 *
 * @receiver Context
 * @param dp Float      the value in dp
 * @return the value of dp to Pixel according to density of the screen
 */
fun Context.dpToPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}

fun AlertDialog.setListeners(positiveListener: (() -> Boolean)? = null, negativeListener: (() -> Boolean)? = null): AlertDialog {

    setOnShowListener {
        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (positiveListener == null) dismiss()
            else if (positiveListener()) dismiss()
        }

        getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            if (negativeListener == null) dismiss()
            else if (negativeListener()) dismiss()
        }
    }

    return this
}