package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.repository.LightRepository

class LightViewModel(app: Application) : AndroidViewModel(app) {
    private val _lifxKey = MutableLiveData<String>()
    val lifxKey: LiveData<String>
        get() = _lifxKey

    private val _listLights = MutableLiveData<List<Light>>()
    val listLights: LiveData<List<Light>>
        get() = _listLights

    private val repository = LightRepository(
        likxKeyChangedListener = {
            _lifxKey.value = it
        }
    )

    fun setLifxKey(key: String): LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        viewModelScope.launch {
            repository.saveLIFXKey(key)
            value = true
        }
    }

    fun fetchLIFXList(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            with(repository.getLIFXList()) {
                if (this == null) result.value = false
                else {
                    _listLights.value = map {
                        Light(
                            name = it.name,
                            uid = it.id,
                            type = it.type,
                            productName = it.productName
                        )
                    }

                    result.value = true
                }
            }
        }

        return result
    }

    fun fetchHueList() = liveData {

        with(repository.getHueList()) {
            if (this == null) emit(false)
            else {
                _listLights.value = map {
                    Light(
                        name = it.name,
                        uid = it.id,
                        type = it.type,
                        productName = it.productName
                    )
                }

                emit(true)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        repository.removeListeners()
    }
}