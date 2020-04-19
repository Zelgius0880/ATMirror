package zelgius.com.atmirror.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.shared.entity.*
import zelgius.com.swipetodelete.SwipeToDeletePagedAdapter

class GroupAdapter(private val editListener: (Group) -> Unit) :
    PagedListAdapter<Any, GroupAdapter.BindableViewHolder<*>>(DIFF_UTIL) {

    override fun getItemViewType(position: Int): Int =
        when {
            getItem(position) is Group -> R.layout.adapter_group
            getItem(position) is Switch -> R.layout.adapter_switch
            getItem(position) is Light -> R.layout.adapter_light
            else -> error("don't know what to do")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<*> =
        when (viewType) {
            R.layout.adapter_group ->
                GroupViewHolder(
                    AdapterGroupBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            R.layout.adapter_switch ->
                SwitchViewHolder(
                    AdapterSwitchBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            R.layout.adapter_light ->
                LightViewHolder(
                    AdapterLightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> error("don't know what to do")
        }

    override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int) {
        when (holder) {

            is SwitchViewHolder -> (getItem(position) as? Switch)?.let {
                holder.bind(it)
            }

            is LightViewHolder -> (getItem(position) as? Light)?.let {
                holder.bind(it)
            }

            is GroupViewHolder -> (getItem(position) as? Group)?.let {
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

    inner class LightViewHolder(private val binder: AdapterLightBinding) :
        BindableViewHolder<Light>(binder.root) {
        override fun bind(item: Light) {
            binder.name.text = item.name
            binder.state.setText(
                when(item.state){
                    Light.State.ON -> R.string.turn_on
                    Light.State.OFF ->  R.string.turn_off
                    Light.State.TOGGLE -> R.string.toggle
                }
            )
        }
    }

    inner class GroupViewHolder(private val binder: AdapterGroupBinding) :
        BindableViewHolder<Group>(binder.root) {
        override fun bind(item: Group) {
            binder.name.text = item.name

            binder.edit.setOnClickListener {
                editListener(item)
            }
        }
    }


    abstract class BindableViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: T)
    }

    override fun onBindViewHolder(
        holder: BindableViewHolder<*>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (holder) {

            is SwitchViewHolder -> (getItem(position) as? Switch)?.let {
                holder.bind(it)
            }

            is LightViewHolder -> (getItem(position) as? Light)?.let {
                holder.bind(it)
            }

            is GroupViewHolder -> (getItem(position) as? Group)?.let {
                holder.bind(it)
            }
        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean =
                when(oldItem) {
                    is Switch -> (newItem as FirebaseObject).key == oldItem.key
                    is Group -> (newItem as FirebaseObject).key == oldItem.key
                    is Light -> (newItem as FirebaseObject).key == oldItem.key
                    else -> false
                }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean =
                oldItem.javaClass.name == newItem.javaClass.name &&
                        when(oldItem) {
                            is Switch -> (newItem as Switch).let {
                                it.uid == oldItem.uid && it.name == oldItem.name
                            }

                            is Group -> (newItem as Group).name == oldItem.name

                            is Light -> (newItem as Light) == oldItem
                            else -> false
                        }

        }
    }
}