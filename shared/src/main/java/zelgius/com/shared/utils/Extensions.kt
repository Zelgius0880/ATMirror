package zelgius.com.shared.utils

import android.content.Context
import androidx.annotation.IntegerRes
import androidx.annotation.RestrictTo
import java.time.ZoneId
import java.util.*
import kotlin.math.round
import android.R.color
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
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
