package zelgius.com.atmirror.compose.custom

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Utils
import zelgius.com.contextextensions.dpToPx

class Chart @JvmOverloads constructor(
    context: Context,
    data: LineData,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    size: Pair<Int, Int> = MATCH_PARENT to MATCH_PARENT,
    legendEnabled: Boolean = false,
    val leftAxis: ChartYAxis? = null,
    val rightAxis: ChartYAxis? = null,
    val x: ChartXAxis? = null,
    backgroundColor: Int? = null,
    extraBottomOffset: Float? = null
) : LineChart(context, attrs, defStyleAttr) {
    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, Paint().apply { isAntiAlias = false })
        layoutParams = MarginLayoutParams(size.first, size.second)
        this.data = data
        legend.isEnabled = legendEnabled

        leftAxis?.applyOnAxis(axisLeft)
        rightAxis?.applyOnAxis(axisRight)
        x?.applyOnAxis(this.xAxis)
        backgroundColor?.let { this.background = ColorDrawable(backgroundColor) }
        description = null

        if(extraBottomOffset != null) this.extraBottomOffset = context.dpToPx(extraBottomOffset)
    }

}

data class ChartYAxis(
    val isEnabled: Boolean = true,
    override val max: Float? = null,
    override val min: Float? = null,
    val granularity: Float? = null,
    override val formatter: ValueFormatter? = null,
    override val axisColor: Int? = null,
    override val textColor: Int? = null,
    override val textSize: Float? = null,
    override val drawGridLines: Boolean? = null,
    override val width: Float? = null
) : BaseChartAxis<YAxis>(axisColor, textColor, formatter, textSize, drawGridLines, width, max, min) {
    override fun applyOnAxis(axis: YAxis) {
        super.applyOnAxis(axis)
        axis.isEnabled = isEnabled

        if(granularity != null) {
            axis.isGranularityEnabled = true
            axis.granularity = granularity
        }
    }


}

data class ChartXAxis(
    val position: XAxis.XAxisPosition = XAxis.XAxisPosition.BOTTOM,
    override val axisColor: Int? = null,
    override val textColor: Int? = null,
    override val formatter: ValueFormatter? = null,
    override val textSize: Float? = null,
    override val drawGridLines: Boolean? = null,
    override val width: Float? = null,
    override val max: Float? = null,
    override val min: Float? = null
) : BaseChartAxis<XAxis>(axisColor, textColor, formatter, textSize, drawGridLines, width, max, min) {


    override fun applyOnAxis(axis: XAxis) {
        super.applyOnAxis(axis)

        axis.position = position
    }
}

open class BaseChartAxis<T : AxisBase>(
    protected open val axisColor: Int? = null,
    protected open val textColor: Int? = null,
    protected open val formatter: ValueFormatter? = null,
    protected open val textSize: Float? = null,
    protected open val drawGridLines: Boolean? = null,
    protected open val width: Float? = null,
    protected open val max: Float? = null,
    protected open val min: Float? = null
) {
    open fun applyOnAxis(axis: T) {
        axisColor?.let { axis.axisLineColor = it }
        textColor?.let { axis.textColor = it }
        formatter?.let { axis.valueFormatter = it }
        textSize?.let { axis.textSize = it }
        drawGridLines?.let { axis.setDrawGridLines(it) }
        width?.let { axis.axisLineWidth = it }
        max?.let { axis.axisMaximum = it }
        min?.let { axis.axisMinimum = it }
    }
}