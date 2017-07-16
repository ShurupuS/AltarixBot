package me.ilich.bigbrother

import android.os.Build
import fi.iki.elonen.NanoHTTPD
import java.util.*

class StatusModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/status.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        return NanoHTTPD.newFixedLengthResponse("${Build.MODEL} here, ${Date()}")
    }

}