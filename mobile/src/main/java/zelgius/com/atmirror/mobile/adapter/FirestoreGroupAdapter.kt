package zelgius.com.atmirror.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.swipetodelete.SwipeToDeleteFirestoreAdapter

class FirestoreGroupAdapter(options: FirestoreRecyclerOptions<Group>) :
    SwipeToDeleteFirestoreAdapter<Group, FirestoreGroupAdapter.BindableViewHolder<*>>(options) {

    override fun getItemViewType(position: Int): Int =
        when {
            //position == 0 -> R.layout.adapter_header
            //position in 1..discoveredSwitches.size + 1 -> R.layout.adapter_switch_discoved
            getData(position) is Group -> R.layout.adapter_group
            //getItem(position) is Switch -> R.layout.adapter_switch
            //getItem(position) is Light -> R.layout.adapter_light
            else -> error("don't know what to do")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder<*> =
        when (viewType) {
            R.layout.adapter_header ->
                HeaderViewHolder(
                    AdapterHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            R.layout.adapter_switch_discoved ->
                DiscoveredViewHolder(
                    AdapterSwitchDiscovedBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
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

/*    override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int) {
        when (holder) {
            is HeaderViewHolder -> getItem(position).let {
                holder.bind(it)
            }

            is DiscoveredViewHolder -> (getItem(position) as? Switch)?.let {
                holder.bind(it)
            }

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

    }*/

/*
    override fun getItem(position: Int): DocumentSnapshot? {
        return when (position) {
            //0 -> null
            //in 1..discoveredSwitches.size + 1 -> discoveredSwitches[position - 1]
            else -> super.getItem(position)
        }
    }
*/


    inner class HeaderViewHolder(private val binder: AdapterHeaderBinding) :
        BindableViewHolder<Any?>(binder.root) {
        override fun bind(item: Any?) {
           /* binder.name.setText(
                if (lastKnownStatus == CurrentStatus.Status.NOT_WORKING)
                    R.string.start_discovering
                else
                    R.string.stop_discovering
            )

            binder.discover.setOnClickListener {
                if (lastKnownStatus == CurrentStatus.Status.NOT_WORKING)
                    networkViewModel.startDiscovery()
                else
                    networkViewModel.startDiscovery()
            }*/
        }
    }

    inner class DiscoveredViewHolder(private val binder: AdapterSwitchDiscovedBinding) :
        BindableViewHolder<Switch>(binder.root) {
        override fun bind(item: Switch) {

        }
    }

    inner class SwitchViewHolder(private val binder: AdapterSwitchBinding) :
        BindableViewHolder<Switch>(binder.root) {
        override fun bind(item: Switch) {

        }
    }

    inner class LightViewHolder(private val binder: AdapterLightBinding) :
        BindableViewHolder<Light>(binder.root) {
        override fun bind(item: Light) {

        }
    }

    inner class GroupViewHolder(private val binder: AdapterGroupBinding) :
        BindableViewHolder<Group>(binder.root) {
        override fun bind(item: Group) {

        }
    }


    abstract class BindableViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: T)
    }

    override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int, model: Group) {
        when (holder) {
            is HeaderViewHolder -> getItem(position).let {
                holder.bind(it)
            }

            is DiscoveredViewHolder -> (getData(position) as? Switch)?.let {
                holder.bind(it)
            }

            is SwitchViewHolder -> (getData(position) as? Switch)?.let {
                holder.bind(it)
            }

            is LightViewHolder -> (getData(position) as? Light)?.let {
                holder.bind(it)
            }

            is GroupViewHolder -> getData(position)?.let {
                holder.bind(it)
            }
        }
    }
}