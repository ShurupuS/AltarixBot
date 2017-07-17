package me.ilich.bigbrother.server.modules

import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.server.HttpServer

abstract class Module(val callback: HttpServer.Callback) {
    abstract fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean
    abstract fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response
}