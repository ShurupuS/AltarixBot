package me.ilich.bigbrother.server.modules

import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.server.HttpServer
import java.util.*

class StatusModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/status.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val allMessages = callback.allMessages()
        val responseJson = Status.Response(allMessages.map { Status.Response.Message(it.id, it.status) })
        val s = callback.parser.toJson(responseJson, Status.Response::class.java)
        return NanoHTTPD.newFixedLengthResponse(s)
    }

}