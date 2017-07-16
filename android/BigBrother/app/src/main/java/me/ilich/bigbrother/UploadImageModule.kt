package me.ilich.bigbrother

import fi.iki.elonen.NanoHTTPD

class UploadImageModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.equals("/upload/image.php", true)

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        //val file = File(files["content"])
        //callback.onImage(file)
        val file = ""
        val response = NanoHTTPD.newFixedLengthResponse("img $file")
        return response
    }

}