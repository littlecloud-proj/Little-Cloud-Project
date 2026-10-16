package com.littlecloud.app.bells

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlecloud.app.MainActivity
import com.littlecloud.app.resources.*
import com.littlecloud.app.ui.*

/** The page that appears over the lock screen while an alarm-style bell rings. */
class RingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        val title = intent.getStringExtra("title") ?: "Little Cloud"
        val body = intent.getStringExtra("body") ?: "Time to pray."
        val opens = intent.getStringExtra("opens")

        setContent {
            LittleCloudTheme(isSystemInDarkTheme()) {
                Column(
                    Modifier.fillMaxSize().background(Lc.bg).systemBarsPadding().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Ic(Res.drawable.ic_bell, 56.dp, Lc.accent)
                    Spacer(Modifier.height(24.dp))
                    Heading(title, 36)
                    Spacer(Modifier.height(10.dp))
                    LText(body, 19, Lc.muted, align = TextAlign.Center)
                    Spacer(Modifier.height(48.dp))
                    if (opens != null) {
                        PrimaryButton("Pray now", icon = Res.drawable.ic_candle) {
                            BellService.stop(this@RingActivity)
                            startActivity(
                                Intent(this@RingActivity, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    .putExtra(MainActivity.EXTRA_OPEN, opens)
                            )
                            finish()
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    OutlineButton("Stop the bell", Modifier.fillMaxWidth()) {
                        BellService.stop(this@RingActivity)
                        finish()
                    }
                }
            }
        }
    }
}
