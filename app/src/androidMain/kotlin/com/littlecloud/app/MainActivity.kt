package com.littlecloud.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.littlecloud.app.bells.BellScheduler
import com.littlecloud.app.data.AppState
import com.littlecloud.app.platform.AndroidPlatform
import com.littlecloud.app.ui.LittleCloudApp

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN = "open"
    }

    private val openRequest = mutableStateOf<String?>(null)
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = AppState()
        openRequest.value = intent?.getStringExtra(EXTRA_OPEN)
        BellScheduler.rescheduleAll(applicationContext)
        val platform = AndroidPlatform(this)
        setContent { LittleCloudApp(state, openRequest, platform) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openRequest.value = intent.getStringExtra(EXTRA_OPEN)
    }

    override fun onResume() {
        super.onResume()
        if (::state.isInitialized) state.refreshDay()
    }
}
