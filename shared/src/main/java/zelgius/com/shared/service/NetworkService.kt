package zelgius.com.shared.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.concurrent.thread


const val NEW_MESSAGE_RECEIVED = "NEW_MESSAGE_RECEIVED"
const val SEND_MESSAGE = 123

class NetworkService : Service() {
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private lateinit var messenger: Messenger
    private var stop = false
    private var socket: MulticastSocket? = null

    /**
     * Handler of incoming messages from clients.
     */
    internal class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            val(what, data) = msg.what to msg.data
            when (what) {
                SEND_MESSAGE -> thread { multicast(data.getByteArray("data")!!) }
                else -> super.handleMessage(msg)
            }
        }

        private fun multicast(bytes: ByteArray) {
            val socket = DatagramSocket()
            val group: InetAddress = InetAddress.getByName("230.1.2.3")
            val packet = DatagramPacket(bytes, bytes.size, group, 4446)
            socket.send(packet)
            socket.close()
        }
    }

    private fun broadcastData(bytes: ByteArray) {
        Intent().also {
            it.action = NEW_MESSAGE_RECEIVED
            it.putExtra("data", bytes)
            sendBroadcast(it)
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        messenger = Messenger(IncomingHandler(this))

        stop = false
        thread {
            socket = MulticastSocket(4446)
            val group: InetAddress =
                InetAddress.getByName("230.1.2.3") // any address between 224.0.0.0 to 239.255.255.255
            socket?.joinGroup(group)

            val buf = ByteArray(256)
            while (!stop && socket != null) {

                val packet = DatagramPacket(buf, buf.size)
                socket?.receive(packet)
                broadcastData(buf.sliceArray(0 until packet.length))
            }
            socket?.leaveGroup(group)
            socket?.close()
            socket = null
        }
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stop = true

        try {
            socket?.close()
            socket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return super.onUnbind(intent)
    }
}