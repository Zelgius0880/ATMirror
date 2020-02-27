package zelgius.com.networking.repository

import android.app.Application
import android.content.*
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import zelgius.com.networking.service.NEW_MESSAGE_RECEIVED
import zelgius.com.networking.service.NetworkService
import zelgius.com.networking.service.SEND_MESSAGE
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkRepository(
    val application: Application,
    var switchListener: (zelgius.com.networking.protocol.NewSwitch) -> Unit = {},
    var startDiscoveryListener: (zelgius.com.networking.protocol.StartDiscovery) -> Unit = {},
    var stopDiscoveryListener: (zelgius.com.networking.protocol.StopDiscovery) -> Unit = {},
    var getCurrentStatusListener: (zelgius.com.networking.protocol.GetCurrentStatus) -> Unit = {},
    var currentStatusListener: (zelgius.com.networking.protocol.CurrentStatus) -> Unit = {}
) {
    private var service: Messenger? = null
    private var bound: Boolean = false

    private var ackCallback: ((zelgius.com.networking.protocol.Ack) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NEW_MESSAGE_RECEIVED -> handleProtocol(zelgius.com.networking.protocol.Protocol.parse(intent.getByteArrayExtra("data")!!))
            }
        }
    }

    private fun handleProtocol(protocol: zelgius.com.networking.protocol.Protocol) {
        when (protocol.code) {
            zelgius.com.networking.protocol.Protocol.Code.ACK -> ackCallback?.invoke(protocol as zelgius.com.networking.protocol.Ack)
            zelgius.com.networking.protocol.Protocol.Code.START_DISCOVERY -> startDiscoveryListener(protocol as zelgius.com.networking.protocol.StartDiscovery)
            zelgius.com.networking.protocol.Protocol.Code.NEW_SWITCH -> switchListener(protocol as zelgius.com.networking.protocol.NewSwitch)
            zelgius.com.networking.protocol.Protocol.Code.STOP_DISCOVERY -> stopDiscoveryListener(protocol as zelgius.com.networking.protocol.StopDiscovery)
            zelgius.com.networking.protocol.Protocol.Code.GET_CURRENT_STATUS -> getCurrentStatusListener(protocol as zelgius.com.networking.protocol.GetCurrentStatus)
            zelgius.com.networking.protocol.Protocol.Code.CURRENT_STATUS -> currentStatusListener(protocol as zelgius.com.networking.protocol.CurrentStatus)
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

    suspend fun sendMessage(protocol: zelgius.com.networking.protocol.Protocol, waitAck: Boolean = false): Boolean {
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