package me.ilich.bigbrother

import android.app.Application
import io.realm.Realm

class BigBrotherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(baseContext)
    }

}