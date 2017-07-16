package me.ilich.bigbrother.server

import fi.iki.elonen.NanoHTTPD

class PublishModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/publish.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val text = session.parameters["text"]?.first()
        val response = if (text == null) {
            NanoHTTPD.newFixedLengthResponse("Specify `text` parameter.")
        } else {
            val message = callback.onText(text)
            NanoHTTPD.newFixedLengthResponse("ok $message")
        }
        return response
    }

}