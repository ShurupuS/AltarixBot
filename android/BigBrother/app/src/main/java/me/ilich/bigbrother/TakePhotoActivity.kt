package me.ilich.bigbrother

import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class TakePhotoActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_ID = "id"

        fun intent(context: Context, id: String): Intent {
            val i = Intent(context, TakePhotoActivity::class.java)
            i.putExtra(EXTRA_ID, id)
            return i
        }

    }

    private lateinit var surfaceView: SurfaceView
    private var camera: Camera? = null
    private lateinit var fileName: String

    private val mPicture = PictureCallback { data, camera ->
        val pictureFile = File(externalCacheDir, "$fileName.jpg")
        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
            finish()
        } catch (e: FileNotFoundException) {
            Log.e("Sokolov", "File not found: " + e.message, e)
        } catch (e: IOException) {
            Log.e("Sokolov", "Error accessing file: " + e.message, e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_take_photo)
        fileName = intent.getStringExtra(EXTRA_ID)
        surfaceView = findViewById(R.id.surface) as SurfaceView
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                camera?.setPreviewDisplay(surfaceHolder)
                camera?.startPreview()
                camera?.takePicture(null, null, mPicture)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        camera = Camera.open(0)
        camera?.startPreview()
/*        val params = camera.parameters
        camera.parameters = params*/
    }

    override fun onPause() {
        super.onPause()
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

}