package zelgius.com.atmirror.shared.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.BuildConfig
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.GroupRepository
import zelgius.com.atmirror.shared.repository.LightRepository
import zelgius.com.atmirror.shared.repository.NetworkRepository
import zelgius.com.atmirror.shared.entity.State
import zelgius.com.utils.toHexString

class MirrorNetworkViewModel(val app: Application) : AndroidViewModel(app) {
    var status: State = State.NOT_WORKING


    private val stateListener: (State) -> Unit = {
            status = it

            viewModelScope.launch {
                when (status) {
                    State.NOT_WORKING -> {
                        repository.stopDiscovery()
                        repository.sendSwitch(null)
                    }
                    State.DISCOVERING -> {
                        repository.startDiscovery()
                    }
                }
            }
    }

    private val repository = NetworkRepository(
        phoneStateChangedListener = stateListener
    )

    private val groupRepository = GroupRepository()
    private val lightRepository = LightRepository()

    fun switchPressed(bytes: ByteArray) {
        if (status == State.DISCOVERING)
            viewModelScope.launch {
                repository.sendSwitch(Switch(bytes))
            }

        else {
            setLightState(bytes.toHexString())
        }
    }

    private fun setLightState(uuid: String) {
        viewModelScope.launch {
            groupRepository.getGroupFromSwitch(uuid).forEach { g ->
                g.lights.forEach {
                    lightRepository.setState(it)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        repository.removeListeners()
    }

}