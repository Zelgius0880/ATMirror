package zelgius.com.atmirror.mobile.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.contextextensions.dpToPx

class LightStatusDropDownAdapter(context: Context) : ArrayAdapter<Light.State>(
    context,
    android.R.layout.simple_expandable_list_item_1,
    arrayOf(Light.State.ON, Light.State.OFF, Light.State.TOGGLE)
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getView(position, convertView, parent) as TextView).apply {
            setText(
                when (getItem(position)!!) {
                    Light.State.ON -> R.string.turn_on
                    Light.State.OFF -> R.string.turn_off
                    Light.State.TOGGLE -> R.string.toggle
                }
            )
        }
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return (super.getDropDownView(position, convertView, parent) as TextView).apply {
            val dp = context.dpToPx(8f)
            setPadding(dp.toInt(),0,dp.toInt(),0)
            setText(
                when (getItem(position)!!) {
                    Light.State.ON -> R.string.turn_on
                    Light.State.OFF -> R.string.turn_off
                    Light.State.TOGGLE -> R.string.toggle
                }
            )
        }
    }
}