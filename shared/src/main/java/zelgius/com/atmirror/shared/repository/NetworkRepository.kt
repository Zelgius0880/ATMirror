package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.getField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.atmirror.shared.entity.FirebaseObject
import zelgius.com.atmirror.shared.entity.Switch

class NetworkRepository(
    var switchListener: (Switch?) -> Unit = {},
    var phoneStateChangedListener: ((State) -> Unit)? = null,
    var mirrorStateChangedListener: ((State) -> Unit)? = null
) {

    private var phoneListenerRegistration: ListenerRegistration? = null
    private var mirrorListenerRegistration: ListenerRegistration? = null
    private var switchListenerRegistration: ListenerRegistration? = null
    val repository = FirebaseRepository()

    private val path = if (mirrorStateChangedListener == null) "mirror" else "phone"

    init {
        CoroutineScope(Dispatchers.Default).launch {
            if (mirrorStateChangedListener != null) {
                setUpMirrorStateListener()
                setUpSwitchStateListener()
            } else { // mirror case -> listening the mirror state
                setUpPhoneStateListener()
            }
        }
    }

     suspend fun startDiscovery() =
        withContext(Dispatchers.Default) {
            repository.createOrUpdate(StateElement(key =path, state = State.DISCOVERING), "states")
        }

    suspend fun stopDiscovery() =
        withContext(Dispatchers.Default) {
            repository.createOrUpdate(StateElement(key = path, state = State.NOT_WORKING), "states")
        }

    suspend fun sendSwitch(switch: Switch?) =
        withContext(Dispatchers.Default) {

            if(switch != null) {
                switch.key = "lastKnownSwitch"
                repository.createOrUpdate(switch, "states")
            } else
                repository.delete("lastKnownSwitch", "states")
        }

    @IgnoreExtraProperties
    data class StateElement(
        @get:Exclude
        override val firebasePath: String = "",
        @get:Exclude
        @set:Exclude
        override var key: String?,
        val state: State) : FirebaseObject

    fun removeListeners() {
        phoneListenerRegistration?.remove()
        mirrorListenerRegistration?.remove()
        switchListenerRegistration?.remove()
    }

    private suspend fun setUpPhoneStateListener() {
        phoneListenerRegistration = repository.listen("phone", "states") { snapshot, exception ->
            exception?.printStackTrace()

            if (snapshot != null && snapshot.getField<String>("state") != null)
                phoneStateChangedListener?.invoke(snapshot.getField("state")!!)
        }
    }


    private suspend fun setUpMirrorStateListener() {
        mirrorListenerRegistration = repository.listen("mirror", "states") { snapshot, exception ->
            exception?.printStackTrace()

            if (snapshot != null && snapshot.getField<String>("state") != null)
                mirrorStateChangedListener?.invoke(snapshot.getField("state")!!)
        }

    }

    private suspend fun setUpSwitchStateListener() {
        mirrorListenerRegistration =
            repository.listen("lastKnownSwitch", "states") { snapshot, exception ->
                exception?.printStackTrace()

                if (snapshot != null) {
                    val switch = snapshot.toObject(Switch::class.java)
                        switchListener.invoke(if(snapshot.exists() && !snapshot.metadata.isFromCache) switch else null)
                }
            }

    }


}

