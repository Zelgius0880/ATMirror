package zelgius.com.shared.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zelgius.com.shared.entity.Switch
import zelgius.com.shared.protocol.CurrentStatus
import zelgius.com.shared.protocol.GetCurrentStatus
import zelgius.com.shared.protocol.StartDiscovery
import zelgius.com.shared.protocol.StopDiscovery
import zelgius.com.shared.repository.NetworkRepository

class PhoneNetworkViewModel(val app: Application) : AndroidViewModel(app){
    private val _status = MutableLiveData<CurrentStatus.Status>(CurrentStatus.Status.NOT_WORKING)
    val status: LiveData<CurrentStatus.Status>
        get() = _status

    private val switchList = mutableListOf<Switch>()
    private val _switches = MutableLiveData<List<Switch>>(switchList)
    val switches: LiveData<List<Switch>>
    get() = _switches

    private val repository = NetworkRepository(app) .apply{
        startDiscoveryListener = {
           error("Cannot do that ${it.code.name}")
        }

        stopDiscoveryListener = {
            error("Cannot do that ${it.code.name}")
        }

        getCurrentStatusListener = {
            error("Cannot do that ${it.code.name}")
        }

        currentStatusListener = {
            _status.postValue(it.status)
        }

        switchListener = {
            Switch(it.uid).let { s ->
                if(!switchList.contains(s)) {
                    switchList.add(s)
                    _switches.postValue(switchList)
                }
            }
        }
    }

    init {
        repository.bind()
    }

    override fun onCleared() {
        super.onCleared()

        repository.unbind()
    }

    fun startDiscovery(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            result.postValue(repository.sendMessage(StartDiscovery(), true))
        }

        return result
    }


    fun stopDiscovery(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            result.postValue(repository.sendMessage(StopDiscovery(), true))
        }

        return result
    }


    fun getCurrentStatus() {
        viewModelScope.launch {
            repository.sendMessage(GetCurrentStatus(), true)
        }
    }
}