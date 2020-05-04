package zelgius.com.atmirror.mobile.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BindableViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}