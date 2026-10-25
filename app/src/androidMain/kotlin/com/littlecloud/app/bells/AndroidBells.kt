package com.littlecloud.app.bells

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.platform.BellSystem
import com.littlecloud.app.ui.LCard
import com.littlecloud.app.ui.LText
import com.littlecloud.app.ui.OutlineButton

/** Bells through AlarmManager and notification channels (see Bells.kt). */
class AndroidBells(private val ctx: Context) : BellSystem {
    override val sounds = Sounds.builtIn.map { BellSystem.Sound(it.key, it.label, it.note) }
    override fun label(key: String) = Sounds.label(key)
    override val canRingLoud = true
    override val footnote = "Tap a bell to change its times or sound. Bells ring through the alarm volume."

    override fun reschedule() = BellScheduler.rescheduleAll(ctx)

    override fun ringNow(name: String, sound: String) =
        BellService.start(ctx, name, "This is how this bell will ring.", sound, null)

    @Composable
    override fun rememberPlayer(): (String?) -> Unit {
        val ctx = LocalContext.current
        val holder = remember { arrayOfNulls<MediaPlayer>(1) }
        DisposableEffect(Unit) { onDispose { holder[0]?.release(); holder[0] = null } }
        return { key ->
            holder[0]?.release(); holder[0] = null
            if (key != null) {
                runCatching {
                    MediaPlayer.create(ctx, Sounds.uri(ctx, key))?.apply {
                        setOnCompletionListener { it.release(); if (holder[0] === it) holder[0] = null }
                        start()
                        holder[0] = this
                    }
                }
            }
        }
    }

    @Composable
    override fun rememberCustomPicker(onPicked: (String) -> Unit): (() -> Unit)? {
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                @Suppress("DEPRECATION")
                val uri: Uri? = res.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) onPicked("custom:$uri")
            }
        }
        return {
            picker.launch(
                Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                    .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            )
        }
    }

    @Composable
    override fun Notices(state: AppState) {
        val ctx = LocalContext.current
        var exactOk by remember { mutableStateOf(BellScheduler.canRingExactly(ctx)) }
        // Check again whenever the app comes back, as after the user allows it in Settings.
        val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(owner) {
            val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
                if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    exactOk = BellScheduler.canRingExactly(ctx)
                    if (exactOk) BellScheduler.rescheduleAll(ctx)
                }
            }
            owner.lifecycle.addObserver(obs)
            onDispose { owner.lifecycle.removeObserver(obs) }
        }
        if (!exactOk && Build.VERSION.SDK_INT >= 31) {
            LCard(padding = 16.dp) {
                LText("Allow alarms so the bells ring on time.", 17)
                Spacer(Modifier.height(4.dp))
                LText("Tap below and turn on \"Allow setting alarms and reminders\" for Little Cloud. Without it, Android may ring the bells several minutes late.", 14, Lc.muted)
                Spacer(Modifier.height(10.dp))
                OutlineButton("Open settings") {
                    runCatching {
                        ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                    }
                    exactOk = BellScheduler.canRingExactly(ctx)
                }
            }
        }

    }
}
