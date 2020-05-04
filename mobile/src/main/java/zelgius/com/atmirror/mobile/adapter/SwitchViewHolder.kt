package zelgius.com.atmirror.mobile.adapter

import zelgius.com.atmirror.mobile.databinding.AdapterSwitchBinding
import zelgius.com.atmirror.shared.entity.Switch

class SwitchViewHolder(private val binder: AdapterSwitchBinding) :
    BindableViewHolder<Switch>(binder.root) {
    override fun bind(item: Switch) {
        binder.name.text = item.name
        binder.uid.text = item.uid
    }
}