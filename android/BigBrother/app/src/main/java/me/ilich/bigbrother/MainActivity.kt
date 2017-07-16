package me.ilich.bigbrother

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri


class MainActivity : AppCompatActivity(), HttpServerService.Listener {

    private var binder: HttpServerService.Binder? = null

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(p0: ComponentName?) {
            binder = null
        }

        override fun onServiceConnected(p0: ComponentName, p1: IBinder?) {
            binder = p1 as HttpServerService.Binder
            binder?.listener = this@MainActivity
        }

    }

    lateinit var messageTextView: TextView
    lateinit var imgMessage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onNewMessage(text: String) {
        runOnUiThread {
            messageTextView.text = text
        }
    }

    override fun onNewImage(file: File) {
        runOnUiThread {
            imgMessage.visibility = View.VISIBLE
            messageTextView.visibility = View.INVISIBLE
            imgMessage.setImageURI(Uri.fromFile(file))
        }
    }

}
