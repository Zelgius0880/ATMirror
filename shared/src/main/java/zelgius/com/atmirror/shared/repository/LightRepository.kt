package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.atmirror.shared.BuildConfig
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.lights.repository.ILight
import zelgius.com.atmirror.shared.repository.lights.LIFXService
import zelgius.com.atmirror.shared.repository.lights.HueService

class LightRepository(
    var likxKeyChangedListener: (String) -> Unit = {}
) : FirebaseRepository() {

    private var lifxKeyListenerRegistration: ListenerRegistration? = null
    private var hueKeyListenerRegistration: ListenerRegistration? = null
    private val lifxRepository =
        LIFXService
    private val hueRepository =
        HueService

    var hueUserName = ""
    suspend fun hueUserName(): String {
        if(hueUserName.isEmpty())
            hueUserName = getSnapshot("key2","states").getString("key")?:""
        return hueUserName
    }

    suspend fun saveLIFXKey(key: String) =
        withContext(Dispatchers.Default) {
            db().collection("states")
                .document("key1")
                .set(mapOf("key" to key))
        }

    suspend fun saveHueKey(key: String) =
        withContext(Dispatchers.Default) {
            db().collection("states")
                .document("key2")
                .set(mapOf("key" to key))
        }

    fun removeListeners() {
        lifxKeyListenerRegistration?.remove()
        hueKeyListenerRegistration?.remove()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            setUpKeyListener()

            lifxRepository.token = getSnapshot("key1","states").getString("key")?:""
            hueUserName = getSnapshot("key2","states").getString("key")?:""
        }
    }

    private suspend fun setUpKeyListener() {
        lifxKeyListenerRegistration =
            listen("key1", "states") { snapshot, exception ->
                exception?.printStackTrace()

                if (snapshot != null) {
                    snapshot.getString("key")?.let {
                        likxKeyChangedListener(it)
                        lifxRepository.token = it
                    }
                }
            }

        hueKeyListenerRegistration =
            listen("key2", "states") { snapshot, exception ->
                exception?.printStackTrace()

                if (snapshot != null) {
                    snapshot.getString("key")?.let {
                        //hueKeyChangedListener(it)
                        hueUserName = it
                    }
                }
            }
    }

    suspend fun getLIFXList() =
        lifxRepository.getLightList()


    suspend fun getHueList() =
        hueRepository.getLightList(hueUserName())

    suspend fun setState(light: Light) {
        when (light.type) {
            ILight.Type.LIFX -> {
                lifxRepository.setLightState(light, light.state)
            }

            ILight.Type.HUE -> {
                hueRepository.setLightState(light, light.state, hueUserName())
            }
        }
    }

}