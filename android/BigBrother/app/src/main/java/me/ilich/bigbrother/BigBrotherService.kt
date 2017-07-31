package me.ilich.bigbrother

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import me.ilich.bigbrother.model.Message
import me.ilich.bigbrother.model.RealmMessage
import me.ilich.bigbrother.server.HttpServer
import me.ilich.bigbrother.server.TAG
import me.ilich.bigbrother.utils.add
import me.ilich.bigbrother.utils.repeatWithDelay
import me.ilich.bigbrother.utils.retryWithDelay
import me.ilich.bigbrother.utils.transactionObservable
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.TimeUnit


class BigBrotherService : Service() {

    companion object {
        const val PORT = 8080
        const val NAME = "Device1"
        const val MESSAGE_LIFETIME_SEC = 30L
    }

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

        override fun onImageFile(file: File, userName: String?): Message {
            TODO("implement onImageFile")
        }

        override fun onImageUrl(imageUrl: String, userName: String?): Message {
            var result: Message? = null
            Observable.just(Unit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap {
                        realm.transactionObservable { realm ->
                            val m = realm.createObject(RealmMessage::class.java)
                            m.type = RealmMessage.TYPE_IMAGE_URL
                            m.imageUrl = imageUrl
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

    }

    private val server = HttpServer(serverCallback, PORT)
    private val binder = Binder()

    private var healthSubscription: Subscription? = null
    private var messageProcessSubscription: Subscription? = null
    private var addressBroadcastSubscription: Subscription? = null

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
        messageProcessSubscription = Observable.just(Unit)
                .map { Date() }
                .flatMap {
                    val obs = listOf(showNextMessage(it), hideNewMessage(it), timer(it))
                    Observable.combineLatest(obs) { }
                }
                .observeOn(Schedulers.computation())
                .repeatWithDelay(1L, TimeUnit.SECONDS)
                .subscribe()
        addressBroadcastSubscription = addressBroadcast()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnError {
                    Log.e(TAG, "broadcast $it")
                }
                .repeatWithDelay(5L, TimeUnit.SECONDS)
                .retryWithDelay(10L, TimeUnit.SECONDS)
                .subscribe({
                    Log.d(TAG, "broadcast $it")
                }, { th ->
                    Log.e(TAG, "broadcast", th)
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
        realm.close()
        server.stop()
        healthSubscription?.unsubscribe()
        messageProcessSubscription?.unsubscribe()
        addressBroadcastSubscription?.unsubscribe()
    }

    fun showNextMessage(now: Date) =
            Observable.just(now)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { Log.v(TAG, "show $now") }
                    .flatMap {
                        realm.transactionObservable { realm ->
                            val messagesToShow = realm.where(RealmMessage::class.java)
                                    .equalTo("status", RealmMessage.STATUS_PUBLISHED, Case.INSENSITIVE)
                                    .findAllSorted("publishAt", Sort.ASCENDING)
                            val visibleMessages = realm.where(RealmMessage::class.java)
                                    .equalTo("status", RealmMessage.STATUS_VISIBLE)
                                    .findAll()
                            Log.v(TAG, "show count=`${messagesToShow.size}`")
                            if (messagesToShow.isNotEmpty() && visibleMessages.isEmpty()) {
                                val msg = messagesToShow.first()
                                msg.status = RealmMessage.STATUS_VISIBLE
                                msg.showAt = now
                            }
                        }
                    }

    fun hideNewMessage(now: Date) =
            Observable.just(now)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { Log.v(TAG, "hide $now") }
                    .flatMap {
                        realm.transactionObservable { realm ->
                            val messagesToHide = realm.where(RealmMessage::class.java)
                                    .equalTo("status", RealmMessage.STATUS_VISIBLE, Case.INSENSITIVE)
                                    .lessThan("showAt", now.add(-MESSAGE_LIFETIME_SEC, TimeUnit.SECONDS))
                                    .findAllSorted("showAt", Sort.ASCENDING)
                            Log.v(TAG, "hide count=`${messagesToHide.size}`")
                            if (messagesToHide.isNotEmpty()) {
                                val msg = messagesToHide.first()
                                msg.status = RealmMessage.STATUS_FINISHED
                                msg.hideAt = now
                            }
                        }
                    }

    fun timer(now: Date) =
            Observable.just(now)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        val visibleMessages = realm.where(RealmMessage::class.java)
                                .equalTo("status", RealmMessage.STATUS_VISIBLE, Case.INSENSITIVE)
                                .findAllSorted("showAt", Sort.ASCENDING)
                        if (visibleMessages.isEmpty()) {
                            binder.presenter?.timerMode(MessagePresenter.TimerMode.OFF)
                        } else {
                            binder.presenter?.timerMode(MessagePresenter.TimerMode.ON)
                            val first = visibleMessages.first()
                            first.showAt?.let {
                                val diff = now.time - it.time
                                val sec = MESSAGE_LIFETIME_SEC - diff / 1000L
                                binder.presenter?.timer(sec)
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

    class Binder : android.os.Binder() {
        var presenter: MessagePresenter? = null
    }

}