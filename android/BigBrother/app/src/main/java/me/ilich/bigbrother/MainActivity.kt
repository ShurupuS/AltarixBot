package me.ilich.bigbrother

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import io.realm.Realm
import io.realm.Sort
import me.ilich.bigbrother.model.RealmMessage
import me.ilich.bigbrother.model.TextMessage
import me.ilich.bigbrother.server.HttpServerService
import rx.Subscription
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var binder: HttpServerService.Binder? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(p0: ComponentName?) {
            binder = null
        }

        override fun onServiceConnected(p0: ComponentName, p1: IBinder?) {
            binder = p1 as HttpServerService.Binder
        }

    }


    private val messagePresenter = object : MessagePresenter {

        override fun displayText(text: String) {
            messageTextView.visibility = View.VISIBLE
            imgMessage.visibility = View.GONE
            messageTextView.text = text
        }

        override fun displayImageFromFile(imageFile: File) {
            messageTextView.visibility = View.GONE
            imgMessage.visibility = View.VISIBLE
            imgMessage.setImageURI(Uri.fromFile(imageFile))
        }

    }

    lateinit var messageTextView: TextView
    lateinit var imgMessage: ImageView
    lateinit var realm: Realm
    private var messageSubscription: Subscription? = null
    private val defaultMessage = TextMessage("default msg")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        bindService(Intent(this, HttpServerService::class.java), serviceConnection, BIND_AUTO_CREATE)
        messageTextView = findViewById(R.id.message) as TextView
        imgMessage = findViewById(R.id.img_message) as ImageView
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        messageSubscription = realm.where(RealmMessage::class.java)
                .findAllSortedAsync("publishAt", Sort.DESCENDING)
                .asObservable()
                .map { realmMessages ->
                    if (realmMessages.isEmpty()) {
                        defaultMessage
                    } else {
                        realmMessages.first().toMessage()
                    }
                }
                .subscribe { message ->
                    Log.d(TAG, "display $message")
                    message.display(messagePresenter)
                }
    }

    override fun onStop() {
        super.onStop()
        messageSubscription?.unsubscribe()
    }

}
