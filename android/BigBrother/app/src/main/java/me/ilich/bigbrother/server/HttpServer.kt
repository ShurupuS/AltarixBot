package me.ilich.bigbrother.server

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File

class HttpServer(val callback: Callback) : NanoHTTPD(8080) {

    private val modules = listOf(
            StatusModule(callback),
            PublishModule(callback)
    )

    override fun serve(session: IHTTPSession): Response {
        Log.v("Sokolov", "uri=`${session.uri}` method=`${session.method}`")
        val module = modules.find { it.isSuitable(session) } ?: NotFoundModule(callback)
        try {
            return module.response(session)
        } catch (th: Throwable) {
            Log.e("Sokolov", th.message, th)
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Error `${th.stackTrace}`")
        }
    }

    interface Callback {
        fun onText(text: String): String
        fun onImage(file: File)
    }

}