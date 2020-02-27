package zelgius.com.networking.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.networking.protocol.CurrentStatus
import zelgius.com.networking.protocol.NewSwitch
import zelgius.com.networking.repository.NetworkRepository

class MirrorNetworkViewModel(val app: Application) : AndroidViewModel(app) {
    private val _status = MutableLiveData<CurrentStatus.Status>(CurrentStatus.Status.NOT_WORKING)
    val status: LiveData<CurrentStatus.Status>
        get() = _status

    private val repository = NetworkRepository(app).apply {
        startDiscoveryListener = {
            _status.postValue(CurrentStatus.Status.SWITCH_DISCOVERING)
        }

        stopDiscoveryListener = {
            _status.postValue(CurrentStatus.Status.NOT_WORKING)
        }

        getCurrentStatusListener = {
            sendCurrentStatus()
        }
    }

    private fun sendCurrentStatus() {
        viewModelScope.launch {
            repository.sendMessage(CurrentStatus(_status.value ?: CurrentStatus.Status.NOT_WORKING))
        }
    }

    fun sendSwitch(bytes: ByteArray) {
        if (_status.value == CurrentStatus.Status.SWITCH_DISCOVERING)
            viewModelScope.launch {
                repository.sendMessage(NewSwitch(bytes))
            }
    }

    init {
        repository.bind()
    }

    override fun onCleared() {
        super.onCleared()

        repository.unbind()
    }

}