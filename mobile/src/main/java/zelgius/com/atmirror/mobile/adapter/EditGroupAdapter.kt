package zelgius.com.atmirror.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import org.jetbrains.annotations.NotNull
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.context
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.FirestoreGroupItemMapper
import zelgius.com.swipetodelete.SwipeToDeleteFirestorePagedAdapter
import zelgius.com.utils.dpToPx

class EditGroupAdapter(
    options: FirestorePagingOptions<GroupItem>,
    val itemChangedListener: (GroupItem) -> Unit
) :
    SwipeToDeleteFirestorePagedAdapter<GroupItem, EditGroupAdapter.BindableViewHolder<*>>(
        GroupItem::class.java,
        options
    ) {

    override fun getItemViewType(position: Int): Int =
        when (FirestoreGroupItemMapper.map(getItem(position)!!)) {
            is Switch -> R.layout.adapter_switch
            is Light -> R.layout.adapter_light_edit
            else -> error("don't know what to do")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<*> =
        when (viewType) {
            R.layout.adapter_switch ->
                SwitchViewHolder(
                    AdapterSwitchBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            R.layout.adapter_light_edit ->
                LightViewHolder(
                    AdapterLightEditBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> error("don't know what to do")
        }


    override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int, model: GroupItem) {
        when (holder) {

            is SwitchViewHolder -> (model as Switch).let {
                holder.bind(it)
            }

            is LightViewHolder -> (model as Light).let {
                holder.bind(it)
            }
        }

    }

    inner class SwitchViewHolder(private val binder: AdapterSwitchBinding) :
        BindableViewHolder<Switch>(binder.root) {
        override fun bind(item: Switch) {
            binder.name.text = binder.root.context.getString(R.string.switch_name_format, item.uid)
        }
    }

    inner class LightViewHolder(private val binder: AdapterLightEditBinding) :
        BindableViewHolder<Light>(binder.root) {
        override fun bind(item: Light) {
            binder.name.text = item.name
            val adapter = LightStatusDropDownAdapter(binder.context)
            binder.spinner.adapter = adapter

            adapter.apply {
                binder.spinner.setSelection(getPosition(item.state))
                binder.spinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            with(getItem(position)!!) {
                                if (this != item.state)
                                    itemChangedListener(item.also { it.state = this})
                            }
                        }
                    }
            }
        }
    }

    abstract class BindableViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: T)
    }
}