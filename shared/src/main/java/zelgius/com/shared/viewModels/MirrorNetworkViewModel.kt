package zelgius.com.shared.viewModels

import android.app.Application
import android.content.*
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.shared.entities.protocol.CurrentStatus
import zelgius.com.shared.entities.protocol.NewSwitch
import zelgius.com.shared.entities.protocol.Protocol
import zelgius.com.shared.repositories.NetworkRepository
import zelgius.com.shared.service.NEW_MESSAGE_RECEIVED
import zelgius.com.shared.service.NetworkService
import zelgius.com.shared.service.SEND_MESSAGE

class MirrorNetworkViewModel(val app: Application) : AndroidViewModel(app){
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

    private fun sendCurrentStatus(){
        viewModelScope.launch {
            repository.sendMessage(CurrentStatus(_status.value ?: CurrentStatus.Status.NOT_WORKING))
        }
    }


    fun sendSwitch(bytes: ByteArray){
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