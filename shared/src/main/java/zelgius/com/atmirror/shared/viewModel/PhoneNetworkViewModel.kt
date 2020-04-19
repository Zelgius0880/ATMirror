package zelgius.com.atmirror.shared.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.protocol.CurrentStatus
import zelgius.com.atmirror.shared.protocol.GetCurrentStatus
import zelgius.com.atmirror.shared.protocol.StartDiscovery
import zelgius.com.atmirror.shared.protocol.StopDiscovery
import zelgius.com.atmirror.shared.repository.NetworkRepository
import zelgius.com.atmirror.shared.repository.State

class PhoneNetworkViewModel(val app: Application) : AndroidViewModel(app) {
    private val _status = MutableLiveData(State.NOT_WORKING)
    val status: LiveData<State>
        get() = _status

    private val _switch = MutableLiveData<Switch?>()
    val switch: LiveData<Switch?>
        get() = _switch

    private val repository = NetworkRepository(
        mirrorStateChangedListener = {
            _status.postValue(it)
        },
        switchListener = {
            _switch.postValue(it?.copy(key = null))
        }
    )

    override fun onCleared() {
        super.onCleared()
        repository.removeListeners()
    }

    fun startDiscovery(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch(CoroutineExceptionHandler { _, e -> throw  e }) {
            repository.startDiscovery()
            result.postValue(true)
        }

        return result
    }


    fun stopDiscovery(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        _switch.value = Switch()

        viewModelScope.launch(CoroutineExceptionHandler { _, e -> throw  e }) {
            repository.stopDiscovery()
            result.postValue(true)
        }

        return result
    }

}