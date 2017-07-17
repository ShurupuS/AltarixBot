package me.ilich.bigbrother.server

import android.util.Log
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.model.Message
import me.ilich.bigbrother.server.modules.NotFoundModule
import me.ilich.bigbrother.server.modules.PublishModule
import me.ilich.bigbrother.server.modules.StatusModule
import java.io.File

class HttpServer(val callback: Callback) : NanoHTTPD(8080) {

    private val modules = listOf(
            PublishModule(callback),
            StatusModule(callback)
    )

    override fun serve(session: IHTTPSession): Response {
        Log.v(TAG, "REQUEST uri=`${session.uri}` method=`${session.method}`")
        val module = modules.find { it.isSuitable(session) } ?: NotFoundModule(callback)
        val response = try {
            module.response(session)
        } catch (th: Throwable) {
            Log.e(TAG, th.message, th)
            NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Error `$th`")
        }
        Log.v(TAG, "RESPONSE `$response`")
        return response
    }

    interface Callback {
        val parser: Gson
        fun onText(text: String): Message
        fun onImage(file: File)
        fun allMessages(): List<Message>
    }

}