package zelgius.com.atmirror.mobile.adapter

import zelgius.com.atmirror.mobile.R
import zelgius.com.atmirror.mobile.databinding.AdapterLightBinding
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.lights.repository.ILight

class LightViewHolder(private val binder: AdapterLightBinding) :
    BindableViewHolder<Light>(binder.root) {
    override fun bind(item: Light) {
        binder.name.text = item.name
        binder.state.setText(
            when(item.state){
                Light.State.ON -> R.string.turn_on
                Light.State.OFF -> R.string.turn_off
                Light.State.TOGGLE -> R.string.toggle
            }
        )

        binder.type.setText( when(item.type) {
            ILight.Type.HUE -> R.string.hue
            ILight.Type.LIFX -> R.string.lifx
        })
    }
}