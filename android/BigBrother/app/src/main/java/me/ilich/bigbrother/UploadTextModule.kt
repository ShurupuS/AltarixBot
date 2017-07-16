package me.ilich.bigbrother

import android.util.Log
import fi.iki.elonen.NanoHTTPD

class UploadTextModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/upload/text.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        Log.v("Sokolov", files.toString())
        Log.v("Sokolov", session.parameters.toString())
        Log.v("Sokolov", session.parms.toString())
        val message = session.parameters["content"]?.first()
        val response = if (message == null) {
            NanoHTTPD.newFixedLengthResponse("specify `content` parameter")
        } else {
            callback.onText(message)
            NanoHTTPD.newFixedLengthResponse("ok")
        }
        return response
        //return NanoHTTPD.newFixedLengthResponse("123")
    }

}