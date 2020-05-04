package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.lights.repository.LIFXService

class LightRepository(
    var likxKeyChangedListener: (String) -> Unit = {}
) : FirebaseRepository() {

    private var lifxKeyListenerRegistration: ListenerRegistration? = null
    private val lifxRepository = LIFXService


    suspend fun saveLIFXKey(key: String) =
        withContext(Dispatchers.Default) {
            db().collection("states")
                .document("key1")
                .set(mapOf("key" to key))
        }

    fun removeListeners() {
        lifxKeyListenerRegistration?.remove()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            setUpKeyListener()
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
    }

    suspend fun getLIFXList() =
        lifxRepository.getLightList()


}