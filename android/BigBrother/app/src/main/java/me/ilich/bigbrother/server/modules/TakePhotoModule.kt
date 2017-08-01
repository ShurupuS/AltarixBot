package me.ilich.bigbrother.server.modules

import fi.iki.elonen.NanoHTTPD
import me.ilich.bigbrother.server.HttpServer
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

class TakePhotoModule(callback: HttpServer.Callback) : Module(callback) {

    override fun isSuitable(session: NanoHTTPD.IHTTPSession): Boolean =
            session.uri.startsWith("/take_photo.php") && session.method == NanoHTTPD.Method.GET

    override fun response(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        var id: String = ""
        Observable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    id = callback.takePhoto()
                }
                .toBlocking()
                .subscribe()
        return NanoHTTPD.newFixedLengthResponse("photo id = `$id`")
    }

}