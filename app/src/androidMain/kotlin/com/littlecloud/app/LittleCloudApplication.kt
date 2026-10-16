package com.littlecloud.app

import android.app.Application
import com.littlecloud.app.platform.AndroidApp

class LittleCloudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidApp.context = this
    }
}
