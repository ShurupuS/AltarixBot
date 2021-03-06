package me.ilich.bigbrother.server

import android.util.Log
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.model.Message
import me.ilich.bigbrother.server.modules.*
import java.io.File
import java.io.InputStream

class HttpServer(val callback: Callback, port: Int) : NanoHTTPD(port) {

    private val modules = listOf(
            PublishModule(callback),
            StatusModule(callback),
            TakePhotoModule(callback),
            PhotoModule(callback)
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
        fun onText(text: String, userName: String?): Message
        fun onImageFile(file: File, userName: String?): Message
        fun onImageUrl(imageUrl: String, userName: String?): Message
        fun allMessages(): List<Message>
        fun takePhoto(): String
        fun file(fileName: String): File
    }

}