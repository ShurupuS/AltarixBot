package me.ilich.bigbrother

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.File
import java.net.NetworkInterface
import java.util.*


class HttpServerService : Service() {

    private val serverCallback = object : HttpServer.Callback {

        override fun onText(text: String) {
            binder.onNewMessage(text)
        }

        override fun onImage(file: File) {
            binder.onNewImage(file)
        }

    }
    private val server = HttpServer(serverCallback)
    private val binder = Binder()

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.v("Sokolov", "onCreate")

        server.start()
        val ip = getIPAddress(true)
        Log.v("Sokolov", "start on $ip")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("Sokolov", "onDestroy")
        server.stop()
    }

    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress()) {
                        val sAddr = addr.getHostAddress()
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }
        // for now eat exceptions
        return ""
    }

    class Binder : android.os.Binder() {

        var listener: Listener? = null

        fun onNewMessage(msg: String) {
            listener?.onNewMessage(msg)
        }

        fun onNewImage(file: File) {
            listener?.onNewImage(file)
        }
    }

    interface Listener {
        fun onNewMessage(text: String)
        fun onNewImage(file: File)
    }

}