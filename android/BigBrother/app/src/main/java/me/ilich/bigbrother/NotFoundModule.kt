package me.ilich.bigbrother

import fi.iki.elonen.NanoHTTPD
import java.util.*

class NotFoundModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean = true

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 not found")
    }

}