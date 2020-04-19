package zelgius.com.atmirror.shared.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import zelgius.com.utils.Networking
import zelgius.com.utils.toHexString
import java.io.IOException
import java.lang.Exception
import java.net.*
import java.nio.ByteBuffer
import kotlin.concurrent.thread


const val NEW_MESSAGE_RECEIVED = "NEW_MESSAGE_RECEIVED"
const val SEND_MESSAGE = 123
const val CLOSE_CONNECTION = 456

class NetworkService : Service() {
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private lateinit var messenger: Messenger
    private var stop = false
    private var socket: Socket? = null
    var server: Boolean = false
    var ip: String? = null

    /**
     * Handler of incoming messages from clients.
     */
    internal class IncomingHandler(val listener: (ByteArray) -> Unit) : Handler() {
        override fun handleMessage(msg: Message) {
            val (what, data) = msg.what to msg.data
            when (what) {
                SEND_MESSAGE -> listener(data.getByteArray("data")!!)
                CLOSE_CONNECTION -> listener(byteArrayOf())
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun broadcastData(bytes: ByteArray) {
        Log.i(NetworkService::class.java.name, "New Message: ${bytes.toHexString()}")
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
        messenger = Messenger(IncomingHandler {
            if(it.isNotEmpty())
                send(it)
            else {
                close()
            }
        })

        stop = false

        val server = intent.getBooleanExtra("SERVER", false)
        if (server) {
            thread {
                val serverSocket = ServerSocket(1234)
                while (true) {
                    Log.i(NetworkService::class.java.name, "Waiting clients")
                    socket = serverSocket.accept()
                    Log.i(NetworkService::class.java.name, "Got new one: ${socket!!.inetAddress}")

                    listening()
                }
            }
        } else {
            thread {
                try {
                    ip = intent.getStringExtra("IP")!!
                    socket = Socket(ip, 1234)
                    listening()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return messenger.binder
    }

    private fun listening() {
        val buf = ByteArray(32)

        val inputStream = socket!!.getInputStream()

        try {
            var size = inputStream.read(buf)
            while (!stop && socket != null && size >= 0) {

                if (size > 0)
                    broadcastData(buf.sliceArray(0 until size))

                size = inputStream.read(buf)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        close()

    }


    override fun onUnbind(intent: Intent?): Boolean {
        stop = true

        close()
        return super.onUnbind(intent)
    }


    private fun send(bytes: ByteArray) {
        thread {
            try {
                checkConnection()
                socket?.getOutputStream()?.apply {
                    write(bytes)
                    flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                close()
            }
        }
    }

    private fun checkConnection() {
        if (!server && ip != null && socket == null) {
            try {
                socket = Socket(ip, 1234)

                thread {
                    listening()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun close() {
        Log.i(NetworkService::class.java.name, "Closing socket ...")
        try {
            socket?.getOutputStream()?.close()
        } catch (e: Exception) { e.printStackTrace() }

        try {
            socket?.getInputStream()?.close()
        } catch (e: Exception) { e.printStackTrace() }

        try {
            socket?.close()
        } catch (e: Exception) { e.printStackTrace() }
        socket = null
    }
}