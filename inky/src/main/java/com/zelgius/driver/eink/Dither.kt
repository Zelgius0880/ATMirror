import android.graphics.Bitmap
import android.graphics.Color
import java.lang.Math.max
import java.lang.Math.min

fun Bitmap.floydSteinbergDithering(colorPalette: IntArray? = null): Bitmap {
    val palette = colorPalette?.map {
        C3(
            r = Color.red(it),
            g = Color.green(it),
            b = Color.blue(it)
        )
    }?.toTypedArray()
        ?: arrayOf(
            C3(0, 0, 0),  // black
            C3(0, 0, 255),  // green
            C3(0, 255, 0),  // blue
            C3(0, 255, 255),  // cyan
            C3(255, 0, 0),  // red
            C3(255, 0, 255),  // purple
            C3(255, 255, 0),  // yellow
            C3(255, 255, 255) // white
        )

    val w = width
    val h = height
    val d = Array(h) { arrayOfNulls<C3>(w) }
    for (y in 0 until h) {
        for (x in 0 until w) {
            with(getPixel(x, y)) {
                d[y][x] = C3(
                    r = Color.red(this),
                    g = Color.green(this),
                    b = Color.blue(this)
                )
            }
        }
    }
    for (y in 0 until height) {
        for (x in 0 until width) {
            val oldColor = d[y][x]
            val newColor = FloydSteinbergDithering.findClosestPaletteColor(oldColor, palette)
            setPixel(x, y, newColor.toColor())

            val err = oldColor!!.sub(newColor)
            if (x + 1 < w) {
                d[y][x + 1] = d[y][x + 1]!!.add(err.mul(7.0 / 16))
            }
            if (x - 1 >= 0 && y + 1 < h) {
                d[y + 1][x - 1] = d[y + 1][x - 1]!!.add(err.mul(3.0 / 16))
            }
            if (y + 1 < h) {
                d[y + 1][x] = d[y + 1][x]!!.add(err.mul(5.0 / 16))
            }
            if (x + 1 < w && y + 1 < h) {
                d[y + 1][x + 1] = d[y + 1][x + 1]!!.add(err.mul(1.0 / 16))
            }
        }
    }
    return this
}

internal class C3(var r: Int, var g: Int, var b: Int) {

    fun add(o: C3): C3 {
        return C3(r + o.r, g + o.g, b + o.b)
    }

    fun clamp(c: Int): Int {
        return max(0, min(255, c))
    }

    fun diff(o: C3?): Int {
        val Rdiff = o!!.r - r
        val Gdiff = o.g - g
        val Bdiff = o.b - b
        return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff
    }

    fun mul(d: Double): C3 {
        return C3((d * r).toInt(), (d * g).toInt(), (d * b).toInt())
    }

    fun sub(o: C3): C3 {
        return C3(r - o.r, g - o.g, b - o.b)
    }

    fun toColor(): Int {
        return Color.rgb(clamp(r), clamp(g), clamp(b))
    }
}

internal object FloydSteinbergDithering {
    fun findClosestPaletteColor(c: C3?, palette: Array<C3>): C3 {
        var closest = palette[0]
        for (n in palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n
            }
        }
        return closest
    }
}