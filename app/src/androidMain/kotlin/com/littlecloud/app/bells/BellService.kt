package com.littlecloud.app.bells

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.littlecloud.app.R

/**
 * Rings a bell like an alarm clock: on the alarm stream, so it sounds even when the
 * ringer is silenced, with a full-screen page over the lock screen.
 */
class BellService : Service() {

    companion object {
        private const val CHANNEL = "bell_ring_alarm"
        private const val NID = 70_001
        const val ACTION_STOP = "com.littlecloud.app.STOP_BELL"

        fun start(ctx: Context, title: String, body: String, sound: String, opens: String?) {
            val i = Intent(ctx, BellService::class.java)
                .putExtra("title", title)
                .putExtra("body", body)
                .putExtra("sound", sound)
                .putExtra("opens", opens)
            ContextCompat.startForegroundService(ctx, i)
        }

        fun stop(ctx: Context) {
            ctx.startService(Intent(ctx, BellService::class.java).setAction(ACTION_STOP))
        }
    }

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP) {
            stopRinging()
            return START_NOT_STICKY
        }
        val title = intent.getStringExtra("title") ?: "Little Cloud"
        val body = intent.getStringExtra("body") ?: "Time to pray."
        val sound = intent.getStringExtra("sound") ?: "angelus"
        val opens = intent.getStringExtra("opens")

        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            val ch = NotificationChannel(CHANNEL, "Bells that ring like an alarm", NotificationManager.IMPORTANCE_HIGH)
            ch.description = "Shown over the lock screen while a bell rings"
            ch.setSound(null, null) // the service plays the bell itself
            nm.createNotificationChannel(ch)
        }

        val ring = Intent(this, RingActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            .putExtra("title", title)
            .putExtra("body", body)
            .putExtra("opens", opens)
        val full = PendingIntent.getActivity(this, NID, ring, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(
            this, NID + 1,
            Intent(this, BellService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(full)
            .setOngoing(true)
            .addAction(0, "Stop the bell", stopPi)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NID, n)
        }
        play(sound)
        return START_NOT_STICKY
    }

    private fun play(sound: String) {
        player?.release()
        player = null
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@BellService, Sounds.uri(this@BellService, sound))
                setOnCompletionListener { stopRinging() }
                prepare()
                start()
            }
        } catch (e: Exception) {
            stopRinging()
        }
    }

    private fun stopRinging() {
        player?.release()
        player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
