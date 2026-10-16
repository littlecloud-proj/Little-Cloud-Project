package com.littlecloud.app.bells

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.littlecloud.app.MainActivity
import com.littlecloud.app.R
import com.littlecloud.app.data.Bell
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object Sounds {
    data class Sound(val key: String, val label: String, val note: String, val raw: Int)

    val builtIn = listOf(
        Sound("angelus", "Angelus bells", "Three, three, three, then nine", R.raw.bell_angelus),
        Sound("peal", "Cathedral peal", "Rounds on eight bells", R.raw.bell_peal),
        Sound("tower", "Single tower bell", "One deep toll", R.raw.bell_tower),
        Sound("hand", "Monastery hand bell", "Soft and brief", R.raw.bell_hand),
    )
    private val clapper = Sound("clapper", "Wooden clapper", "For the Sacred Triduum", R.raw.bell_clapper)

    fun uri(ctx: Context, key: String): Uri {
        if (key.startsWith("custom:")) return Uri.parse(key.removePrefix("custom:"))
        val s = (builtIn + clapper).firstOrNull { it.key == key } ?: builtIn.first()
        return Uri.parse("android.resource://${ctx.packageName}/${s.raw}")
    }

    fun label(key: String): String =
        if (key.startsWith("custom:")) "Your ringtone"
        else (builtIn + clapper).firstOrNull { it.key == key }?.label ?: "Bell"

    /** Notification channels carry their own sound, so each sound gets its own channel. */
    fun channel(ctx: Context, key: String): String {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val id = if (key.startsWith("custom:")) "bell_c" + Integer.toHexString(key.hashCode()) else "bell_$key"
        if (nm.getNotificationChannel(id) == null) {
            val ch = NotificationChannel(id, "Bells · " + label(key), NotificationManager.IMPORTANCE_HIGH)
            ch.description = "Reminders to pray"
            ch.setSound(
                uri(ctx, key),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            ch.enableVibration(true)
            nm.createNotificationChannel(ch)
        }
        return id
    }
}

object BellScheduler {
    private const val KEY_CODES = "scheduledCodes"
    const val TIMER_CODE = 99_001

    fun rescheduleAll(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val prefs = Store.prefs(ctx)
        prefs.getStringSet(KEY_CODES, emptySet())?.forEach { code ->
            code.toIntOrNull()?.let { am.cancel(pending(ctx, it, Intent(ctx, BellReceiver::class.java))) }
        }
        val codes = mutableSetOf<String>()
        Store.loadBells(ctx).filter { it.enabled }.forEach { bell ->
            bell.times.indices.forEach { idx ->
                schedule(ctx, bell, idx)
                codes += code(bell, idx).toString()
            }
        }
        prefs.edit().putStringSet(KEY_CODES, codes).apply()
    }

    private fun code(bell: Bell, idx: Int) = bell.id * 100 + idx

    private fun pending(ctx: Context, code: Int, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(ctx, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun schedule(ctx: Context, bell: Bell, idx: Int) {
        val minutes = bell.times[idx]
        val now = LocalDateTime.now()
        var at = now.toLocalDate().atTime(minutes / 60, minutes % 60)
        if (!at.isAfter(now)) at = at.plusDays(1)
        val intent = Intent(ctx, BellReceiver::class.java)
            .putExtra("bell", bell.id)
            .putExtra("idx", idx)
        setAlarm(ctx, at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pending(ctx, code(bell, idx), intent))
    }

    fun startPrayerTimer(ctx: Context, minutes: Int): Long {
        val at = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(ctx, BellReceiver::class.java).putExtra("timer", true)
        setAlarm(ctx, at, pending(ctx, TIMER_CODE, intent))
        return at
    }

    fun cancelPrayerTimer(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.cancel(pending(ctx, TIMER_CODE, Intent(ctx, BellReceiver::class.java).putExtra("timer", true)))
    }

    fun canRingExactly(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        return ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(ctx: Context, atMillis: Long, pi: PendingIntent) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        if (canRingExactly(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        }
    }
}

class BellReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.getBooleanExtra("timer", false)) {
            notify(ctx, "Mental prayer", "Your time of prayer has ended.", "hand", BellScheduler.TIMER_CODE, "intimacy")
            return
        }
        val id = intent.getIntExtra("bell", -1)
        val idx = intent.getIntExtra("idx", 0)
        val bell = Store.loadBells(ctx).firstOrNull { it.id == id } ?: return
        if (!bell.enabled || idx >= bell.times.size) return

        val today = LocalDate.now()
        val silent = Store.triduumSilence(ctx) && Calendar1962.isTriduum(today)
        val sound = if (silent) "clapper" else bell.sound

        var title = bell.name
        var opens = bell.opens
        var body = when (bell.opens) {
            "office" -> "Time to pray the Little Office."
            "intimacy" -> "Time for mental prayer."
            "evening" -> "Time for night prayers."
            else -> "Time to pray."
        }
        if (bell.opens == "prayer:angelus") {
            if (Calendar1962.isPaschaltide(today)) {
                title = "Regina Cæli"; body = "Queen of Heaven, rejoice, alleluia."; opens = "prayer:regina_caeli"
            } else {
                body = "The Angel of the Lord declared unto Mary."
            }
        }
        if (bell.loud) {
            BellService.start(ctx, title, body, sound, opens)
        } else {
            notify(ctx, title, body, sound, bell.id * 100 + idx, opens)
        }
        BellScheduler.schedule(ctx, bell, idx)
    }

    @SuppressLint("MissingPermission")
    private fun notify(ctx: Context, title: String, body: String, sound: String, nid: Int, opens: String?) {
        val nmc = NotificationManagerCompat.from(ctx)
        if (!nmc.areNotificationsEnabled()) return
        val open = Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(MainActivity.EXTRA_OPEN, opens)
        val content = PendingIntent.getActivity(ctx, nid, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, Sounds.channel(ctx, sound))
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(content)
            .setAutoCancel(true)
            .build()
        nmc.notify(nid, n)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        BellScheduler.rescheduleAll(ctx)
    }
}
