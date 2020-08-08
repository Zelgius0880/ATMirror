package zelgius.com.atmirror.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point

fun Bitmap.resample() {
    // First we look for the most prominent colors
    // i.e. They make up at least 1% of the image
    val stats = mutableMapOf<Int, Int>()

    for (x in 0 until width) {
        for (y in 0 until height) {
            val px = getPixel(x, y)
            if (Color.alpha(px) == 0)
                continue;

            val pxS = px or 0xFF000000.toInt()
            if (stats.containsKey(pxS))
                stats[pxS] = stats[pxS]!! + 1
            else
                stats[pxS] = 1
        }
    }

    val totalSize = width * height / 1000
    val minAccepted = 0.01f
    val selectedColors = mutableListOf<Int>()

    // Make up a list with the selected colors
    stats.forEach { (t, u) ->
        if ((u.toFloat() / totalSize) > minAccepted)
            selectedColors.add(t);
    }

    // Keep growing the zones with the selected colors to cover the invalid colors created by the anti-aliasing
    while (growSelected(selectedColors));
}

private fun Bitmap.growSelected(selectedColors: List<Int>): Boolean {

    for (x in 0 until width) {
        for (y in 0 until height) {
            val px = getPixel(x, y)
            if (Color.alpha(px) == 0)
                continue

            val pxS = 0xFF000000.toInt() or px

            if (selectedColors.contains(pxS)) {
                if (!isBackedByNeighbors(x, y))
                    continue

                val neighbors = getNeighbors(x, y);
                neighbors.forEach {
                    val n = getPixel(it.x, it.y)
                    if (!isBackedByNeighbors(it.x, it.y))
                        setPixel(
                            it.x, it.y,
                            Color.argb(
                                Color.alpha(n),
                                Color.red(pxS),
                                Color.green(pxS),
                                Color.blue(pxS),
                            )
                        )
                }
            } else {
                return true
            }
        }
    }

    return false
}

private fun Bitmap.getNeighbors(x: Int, y: Int): List<Point> {
    val neighbors = mutableListOf<Point>()

    var i = x - 1
    while (i > 0 && i <= x + 1 && i < width){
        var j = y - 1
        while (j > 0 && j <= y + 1 && j < height) {
            neighbors.add(Point(i, j))
            ++j
        }
        ++i
    }

    return neighbors
}

private fun Bitmap.isBackedByNeighbors(x: Int, y: Int): Boolean {
    val neighbors = getNeighbors(x, y)
    val px = getPixel(x, y)
    var similar = 0
    neighbors.forEach {
        val n = getPixel(it.x, it.y)
        if (0xFF000000.toInt() or px == 0xFF000000.toInt() or n)
            similar++;
    }

    return (similar > 2);
}