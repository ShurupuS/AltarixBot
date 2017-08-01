package me.ilich.bigbrother.server.modules

import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.server.HttpServer

class PhotoModule(callback: HttpServer.Callback) : Module(callback) {

    companion object {
        private const val URL = "/photo/"
    }

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.startsWith(URL, true) && session.method == NanoHTTPD.Method.GET

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val fileName = session.uri.substring(URL.length)
        val file = callback.file(fileName)
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/jpeg", file.inputStream(), file.length())
    }

}