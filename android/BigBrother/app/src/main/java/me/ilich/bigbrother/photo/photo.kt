package me.ilich.bigbrother.photo

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import java.io.IOException

fun takePhoto(context: Context) {
    val preview = SurfaceView(context)
    val holder = preview.holder
    // deprecated setting, but required on Android versions prior to 3.0
    //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

    holder.addCallback(object : SurfaceHolder.Callback {
        //The preview must happen at or after this point or takePicture fails
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d("Sokolov", "Surface created")

            var camera: Camera? = null

            try {
                camera = Camera.open()
                Log.d("Sokolov", "Opened camera")

                try {
                    camera!!.setPreviewDisplay(holder)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }

                camera!!.startPreview()
                Log.d("Sokolov", "Started preview")

                camera!!.takePicture(null, null, PictureCallback { data, camera ->
                    Log.d("Sokolov", "Took picture")
                    camera.release()
                })
            } catch (e: Exception) {
                if (camera != null)
                    camera!!.release()
                throw RuntimeException(e)
            }

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    })

    val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val params = WindowManager.LayoutParams(
            1, 1, //Must be at least 1x1
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            0,
            //Don't know if this is a safe default
            PixelFormat.UNKNOWN)

    //Don't set the preview visibility to GONE or INVISIBLE
    wm.addView(preview, params)
}