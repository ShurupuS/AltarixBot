package me.ilich.bigbrother

import fi.iki.elonen.NanoHTTPD

abstract class Module(val callback: HttpServer.Callback) {
    abstract fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean
    abstract fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response
}