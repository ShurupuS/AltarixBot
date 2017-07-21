package me.ilich.bigbrother.server.modules

import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.server.HttpServer

class PublishModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/publish.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val userName = session.parameters["user_name"]?.first()
        val text = session.parameters["text"]?.first()
        val imageUrl = session.parameters["image_url"]?.first()
        val response = when {
            text != null && imageUrl == null -> {
                val message = callback.onText(text, userName)
                NanoHTTPD.newFixedLengthResponse("ok text ${message.id}")
            }
            text == null && imageUrl != null -> {
                val message = callback.onImageUrl(imageUrl, userName)
                NanoHTTPD.newFixedLengthResponse("ok image ${message.id}")
            }
            else -> {
                NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Specify `text` or `image_url` parameter, but not both.")
            }
        }
        return response
    }

}