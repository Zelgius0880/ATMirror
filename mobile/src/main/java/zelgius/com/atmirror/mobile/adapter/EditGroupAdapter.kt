package zelgius.com.atmirror.mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import org.jetbrains.annotations.NotNull
import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.context
import zelgius.com.atmirror.mobile.databinding.*
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.FirestoreGroupItemMapper
import zelgius.com.lights.repository.ILight
import zelgius.com.swipetodelete.SwipeToDeleteFirestorePagedAdapter
import zelgius.com.utils.dpToPx

class EditGroupAdapter(
    options: FirestorePagingOptions<GroupItem>,
    val itemChangedListener: (GroupItem) -> Unit,
    val itemRemovedListener: (GroupItem) -> Unit
) :
    SwipeToDeleteFirestorePagedAdapter<GroupItem, EditGroupAdapter.BindableViewHolder<*>>(
        GroupItem::class.java,
        options,
        deleteListener = { itemRemovedListener(it) }
    ) {

    private val _loadingStatus = MutableLiveData<LoadingState>()
    val loadingStatus: LiveData<LoadingState>
        get() = _loadingStatus

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

    override fun onLoadingStateChanged(state: LoadingState) {
        super.onLoadingStateChanged(state)
        _loadingStatus.postValue(state)
    }

    override fun onBindViewHolder(holder: BindableViewHolder<*>, position: Int, model: GroupItem) {
        if (model != null) {
            when (holder) {

                is SwitchViewHolder -> (model as Switch).let {
                    holder.bind(it)
                }

                is LightViewHolder -> (model as Light).let {
                    holder.bind(it)
                }
            }
        }

    }

    override fun getData(position: Int): GroupItem? {
        return FirestoreGroupItemMapper.map(getItem(position)!!)
    }

    inner class SwitchViewHolder(private val binder: AdapterSwitchBinding) :
        BindableViewHolder<Switch>(binder.root) {
        override fun bind(item: Switch) {
            binder.name.text = item.name
            binder.uid.text = item.uid
        }
    }

    inner class LightViewHolder(private val binder: AdapterLightEditBinding) :
        BindableViewHolder<Light>(binder.root) {
        override fun bind(item: Light) {
            binder.name.text = item.name
            val adapter = LightStatusDropDownAdapter(binder.context)
            binder.spinner.adapter = adapter

            binder.type.setText(
                when (item.type) {
                    ILight.Type.HUE -> R.string.hue
                    ILight.Type.LIFX -> R.string.lifx
                }
            )

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
                                    itemChangedListener(item.also { it.state = this })
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