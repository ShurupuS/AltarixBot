package me.ilich.bigbrother

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.realm.Realm
import io.realm.Sort
import me.ilich.bigbrother.model.RealmMessage
import me.ilich.bigbrother.model.StubMessage
import me.ilich.bigbrother.server.HttpServerService
import rx.Subscription


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

        override fun mode(mode: MessagePresenter.Mode) {
            when (mode) {
                MessagePresenter.Mode.TEXT -> {
                    messageClarificationTextView.visibility = View.GONE
                    messageTextTextView.visibility = View.VISIBLE
                    messageImageImageView.visibility = View.GONE
                    altarixBoxTextView.visibility = View.GONE

                }
                MessagePresenter.Mode.IMAGE -> {
                    messageClarificationTextView.visibility = View.GONE
                    messageTextTextView.visibility = View.GONE
                    messageImageImageView.visibility = View.VISIBLE
                    altarixBoxTextView.visibility = View.VISIBLE
                }
                MessagePresenter.Mode.CLARIFICATION -> {
                    messageClarificationTextView.visibility = View.VISIBLE
                    messageTextTextView.visibility = View.GONE
                    messageImageImageView.visibility = View.GONE
                    altarixBoxTextView.visibility = View.GONE
                }
            }
        }

        override fun messageText(message: String) {
            messageTextTextView.text = message
        }

        override fun messageImageUrl(imageUrl: String) {
            Glide.with(this@MainActivity).load(imageUrl).into(messageImageImageView)
        }

        override fun timer(seconds: Long) {
            timerTextView.text = "$seconds"
        }

        override fun userName(userName: List<String>) {
            when (userName.size) {
                0 -> {
                    userNameCurrentTextView.visibility = View.GONE
                    userNameNextTextView.visibility = View.GONE
                    userNameNextNextTextView.visibility = View.GONE
                }
                1 -> {
                    userNameCurrentTextView.visibility = View.VISIBLE
                    userNameNextTextView.visibility = View.GONE
                    userNameNextNextTextView.visibility = View.GONE
                    userNameCurrentTextView.text = userName[0]
                }
                2 -> {
                    userNameCurrentTextView.visibility = View.VISIBLE
                    userNameNextTextView.visibility = View.VISIBLE
                    userNameNextNextTextView.visibility = View.GONE
                    userNameCurrentTextView.text = userName[0]
                    userNameNextTextView.text = userName[1]
                }
                else -> {
                    userNameCurrentTextView.visibility = View.VISIBLE
                    userNameNextTextView.visibility = View.VISIBLE
                    userNameNextNextTextView.visibility = View.VISIBLE
                    userNameCurrentTextView.text = userName[0]
                    userNameNextTextView.text = userName[1]
                    userNameNextNextTextView.text = userName[2]
                }
            }
        }

    }

    lateinit var timerTextView: TextView
    lateinit var userNameCurrentTextView: TextView
    lateinit var userNameNextTextView: TextView
    lateinit var userNameNextNextTextView: TextView
    lateinit var messageTextTextView: TextView
    lateinit var messageImageImageView: ImageView
    lateinit var messageClarificationTextView: TextView
    lateinit var altarixBoxTextView: TextView

    lateinit var realm: Realm
    private var messageSubscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        bindService(Intent(this, HttpServerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        timerTextView = findViewById(R.id.timer) as TextView
        userNameCurrentTextView = findViewById(R.id.user_name_current) as TextView
        userNameNextTextView = findViewById(R.id.user_name_next) as TextView
        userNameNextNextTextView = findViewById(R.id.user_name_next_next) as TextView
        messageTextTextView = findViewById(R.id.message_text) as TextView
        messageImageImageView = findViewById(R.id.message_image) as ImageView
        messageClarificationTextView = findViewById(R.id.clarification) as TextView
        altarixBoxTextView = findViewById(R.id.altarix_bot) as TextView
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
