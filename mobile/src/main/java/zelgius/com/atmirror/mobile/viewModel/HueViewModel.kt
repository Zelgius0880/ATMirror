package zelgius.com.atmirror.mobile.viewModel

import android.app.Application
import androidx.lifecycle.*
import zelgius.com.atmirror.shared.BuildConfig
import zelgius.com.atmirror.shared.repository.LightRepository
import zelgius.com.atmirror.shared.repository.lights.HueService

class HueViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = HueService
    private val lightRepository = LightRepository()

    fun checkRegistering() = liveData {
        emit(repository.connect(lightRepository.hueUserName()) == null)
    }

    fun register() = liveData{

        with(repository.register()) {
            if(this !== null)
                lightRepository.saveHueKey(this)
            emit(this != null)
        }
    }

}