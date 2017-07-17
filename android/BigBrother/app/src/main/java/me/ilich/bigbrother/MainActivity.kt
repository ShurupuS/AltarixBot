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
import me.ilich.bigbrother.model.StubMessage
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

        override fun displayTextWithClarification(text: String) {
            textMessage.visibility = View.VISIBLE
            imageMessage.visibility = View.GONE
            textMessage.text = text
            clarification.visibility = View.VISIBLE
        }

        override fun displayText(text: String) {
            textMessage.visibility = View.VISIBLE
            imageMessage.visibility = View.GONE
            textMessage.text = text
            clarification.visibility = View.GONE
        }

        override fun displayImageFromFile(imageFile: File) {
            textMessage.visibility = View.GONE
            imageMessage.visibility = View.VISIBLE
            imageMessage.setImageURI(Uri.fromFile(imageFile))
            clarification.visibility = View.VISIBLE
        }

    }

    lateinit var textMessage: TextView
    lateinit var imageMessage: ImageView
    lateinit var clarification: TextView

    lateinit var realm: Realm
    private var messageSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        bindService(Intent(this, HttpServerService::class.java), serviceConnection, BIND_AUTO_CREATE)
        textMessage = findViewById(R.id.message_text) as TextView
        imageMessage = findViewById(R.id.message_image) as ImageView
        clarification = findViewById(R.id.clarification) as TextView
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        messageSubscription = realm.where(RealmMessage::class.java)
                .equalTo("status", RealmMessage.STATUS_VISIBLE)
                .findAllSortedAsync("showAt", Sort.DESCENDING)
                .asObservable()
                .map { realmMessages ->
                    realmMessages?.firstOrNull()?.toMessage() ?: StubMessage()
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
