package me.ilich.bigbrother

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.util.*

class HttpServerService : Service() {

    private val server = Server()
    private val binder = Binder()

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.v("Sokolov", "onCreate")
        server.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("Sokolov", "onDestroy")
        server.stop()
    }

    inner class Server : NanoHTTPD(8080) {

        override fun serve(session: IHTTPSession): Response {
            Log.v("Sokolov", session.uri)
            val response = when (session.uri) {
                "/message" -> {
                    val message = session.parameters["text"]?.first()
                    if (message == null) {
                        newFixedLengthResponse("specify `text` parameter")
                    } else {
                        Log.d("Sokolov", message)
                        binder.onNewMessage(message)
                        newFixedLengthResponse("ok")
                    }
                }
                else -> newFixedLengthResponse("${android.os.Build.MODEL} here, ${Date()}")
            }
            return response
        }
    }

    class Binder : android.os.Binder() {

        var listener: Listener? = null

        fun onNewMessage(msg: String) {
            listener?.onNewMessage(msg)
        }
    }

    interface Listener {
        fun onNewMessage(text: String)
    }

}