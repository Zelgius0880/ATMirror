package zelgius.com.shared.repositories

import android.app.Application
import android.content.*
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import zelgius.com.shared.entities.protocol.*
import zelgius.com.shared.service.NEW_MESSAGE_RECEIVED
import zelgius.com.shared.service.NetworkService
import zelgius.com.shared.service.SEND_MESSAGE
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkRepository(
    val application: Application,
    var switchListener: (NewSwitch) -> Unit = {},
    var startDiscoveryListener: (StartDiscovery) -> Unit = {},
    var stopDiscoveryListener: (StopDiscovery) -> Unit = {},
    var getCurrentStatusListener: (GetCurrentStatus) -> Unit = {},
    var currentStatusListener: (CurrentStatus) -> Unit = {}
) {
    private var service: Messenger? = null
    private var bound: Boolean = false

    private var ackCallback: ((Ack) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NEW_MESSAGE_RECEIVED -> handleProtocol(Protocol.parse(intent.getByteArrayExtra("data")))
            }
        }
    }

    private fun handleProtocol(protocol: Protocol) {
        when (protocol.code) {
            Protocol.Code.ACK -> ackCallback?.invoke(protocol as Ack)
            Protocol.Code.START_DISCOVERY -> startDiscoveryListener(protocol as StartDiscovery)
            Protocol.Code.NEW_SWITCH -> switchListener(protocol as NewSwitch)
            Protocol.Code.STOP_DISCOVERY -> stopDiscoveryListener(protocol as StopDiscovery)
            Protocol.Code.GET_CURRENT_STATUS -> getCurrentStatusListener(protocol as GetCurrentStatus)
            Protocol.Code.CURRENT_STATUS -> currentStatusListener(protocol as CurrentStatus)
        }
    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            this@NetworkRepository.service = Messenger(service)
            bound = true

        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            service = null
            bound = false
        }
    }

    fun bind() {
        Intent(application, NetworkService::class.java).also { intent ->
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        application.registerReceiver(receiver, IntentFilter())

        bound = true
    }

    fun unbind() {
        if (bound) {
            application.unbindService(connection)
            bound = false
        }

        application.unregisterReceiver(receiver)
    }

    suspend fun sendMessage(protocol: Protocol, waitAck: Boolean = false): Boolean {
        if (!bound) return false
        // Create and send a message to the service, using a supported 'what' value
        val msg = Message.obtain(null, SEND_MESSAGE, 0, 0).apply {
            data = bundleOf("data" to protocol.build())
        }

        try {
            service?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        return if (waitAck) {
            suspendCoroutine { cont ->
                ackCallback = {
                    cont.resume(it.ackOk)
                    ackCallback = null
                }
            }
        } else {
            true
        }
    }
}