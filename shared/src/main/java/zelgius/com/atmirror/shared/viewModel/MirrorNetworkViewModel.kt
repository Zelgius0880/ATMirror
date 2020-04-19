package zelgius.com.atmirror.shared.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.protocol.CurrentStatus
import zelgius.com.atmirror.shared.repository.NetworkRepository
import zelgius.com.atmirror.shared.repository.State

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


    fun switchPressed(bytes: ByteArray) {
        if (status == State.DISCOVERING)
            viewModelScope.launch {
                repository.sendSwitch(Switch(bytes))
            }
    }

    override fun onCleared() {
        super.onCleared()

        repository.removeListeners()
    }

}