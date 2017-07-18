package me.ilich.bigbrother.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.Sort
import me.ilich.bigbrother.model.Message
import me.ilich.bigbrother.model.RealmMessage
import me.ilich.bigbrother.utils.add
import me.ilich.bigbrother.utils.repeatWithDelay
import me.ilich.bigbrother.utils.transactionObservable
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.TimeUnit


class HttpServerService : Service() {

    private lateinit var realm: Realm

    private val serverCallback = object : HttpServer.Callback {

        override val parser: Gson = GsonBuilder().create()

        override fun allMessages(): List<Message> {
            var result: List<Message>? = null
            Observable.just(Unit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map {
                        realm.where(RealmMessage::class.java)
                                .findAllSorted("publishAt", Sort.DESCENDING)
                    }
                    .map {
                        it.map { it.toMessage() }
                    }
                    .toBlocking()
                    .subscribe {
                        result = it
                    }
            return result!!
        }

        override fun onText(text: String, userName: String?): Message {
            var result: Message? = null
            Observable.just(Unit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        realm.transactionObservable { realm ->
                            val m = realm.createObject(RealmMessage::class.java)
                            m.type = RealmMessage.TYPE_TEXT
                            m.text = text
                            m.status = RealmMessage.STATUS_PUBLISHED
                            m.publishAt = Date()
                            m.userName = userName
                            result = m.toMessage()
                        }
                    }
                    .toBlocking()
                    .subscribe()
            return result!!
        }

        override fun onImage(file: File, userName: String?) {

        }

    }

    private val server = HttpServer(serverCallback)
    private val binder = Binder()

    private var healthSubscription: Subscription? = null
    private var showMessageSubscription: Subscription? = null
    private var hideMessageSubscription: Subscription? = null

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate")
        realm = Realm.getDefaultInstance()

        server.start()

        healthSubscription = Observable.interval(5000L, TimeUnit.MILLISECONDS)
                .subscribe {
                    val ip = getIPAddress(true)
                    Log.i(TAG, "Health: $ip:${server.listeningPort} alive=`${server.isAlive}` host=`${server.hostname}` wasStarted=`${server.wasStarted()}`")
                }
        showMessageSubscription = Observable.just(Unit)
                .map { Date() }
                .showNextMessage()
                .observeOn(Schedulers.computation())
                .repeatWithDelay(10L, TimeUnit.SECONDS)
                .subscribe()
        hideMessageSubscription = Observable.just(Unit)
                .map { Date() }
                .hideNewMessage()
                .observeOn(Schedulers.computation())
                .repeatWithDelay(10L, TimeUnit.SECONDS)
                .subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
        realm.close()
        server.stop()
        healthSubscription?.unsubscribe()
        showMessageSubscription?.unsubscribe()
        hideMessageSubscription?.unsubscribe()
    }

    fun Observable<Date>.showNextMessage() =
            observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { Log.v(TAG, "show $it") }
                    .flatMap { now ->
                        realm.transactionObservable { realm ->
                            val messagesToShow = realm.where(RealmMessage::class.java)
                                    .equalTo("status", RealmMessage.STATUS_PUBLISHED)
                                    .findAllSorted("publishAt", Sort.ASCENDING)
                            Log.v(TAG, "show count=`${messagesToShow.size}`")
                            if (messagesToShow.isNotEmpty()) {
                                val msg = messagesToShow.first()
                                msg.status = RealmMessage.STATUS_VISIBLE
                                msg.showAt = now
                            }
                        }
                    }

    fun Observable<Date>.hideNewMessage() =
            observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { Log.v(TAG, "hide $it") }
                    .flatMap { now ->
                        realm.transactionObservable { realm ->
                            val messagesToHide = realm.where(RealmMessage::class.java)
                                    .equalTo("status", RealmMessage.STATUS_VISIBLE)
                                    .lessThan("showAt", now.add(-10L, TimeUnit.SECONDS))
                                    .findAllSorted("showAt", Sort.ASCENDING)
                            Log.v(TAG, "hide count=`${messagesToHide.size}`")
                            if (messagesToHide.isNotEmpty()) {
                                val msg = messagesToHide.first()
                                msg.status = RealmMessage.STATUS_FINISHED
                                msg.hideAt = now
                            }
                        }
                    }


    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.getInetAddresses())
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress()) {
                        val sAddr = addr.getHostAddress()
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }
        // for now eat exceptions
        return ""
    }

    class Binder : android.os.Binder()

}