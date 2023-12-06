package zelgius.com.atmirror.mobile


fun IntRange.convert(number: Int, target: IntRange): Int {
    val ratio = number.toFloat() / (endInclusive - start)
    return (ratio * (target.last - target.first)).toInt()
}

fun IntRange.convert(number: Float, target: IntRange): Float {
    val ratio = number / (endInclusive - start)
    return (ratio * (target.last - target.first))
}
