package com.littlecloud.app.ui

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecloud.app.R
import com.littlecloud.app.bells.BellScheduler
import com.littlecloud.app.bells.Sounds
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Bell

/** Plays a short preview of a bell sound; stops when the screen goes away. */
@Composable
fun rememberPreviewPlayer(): (String?) -> Unit {
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
fun BellsScreen(state: AppState, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    val ctx = LocalContext.current
    val play = rememberPreviewPlayer()
    var editing by remember { mutableStateOf<Bell?>(null) }
    var exactOk by remember { mutableStateOf(BellScheduler.canRingExactly(ctx)) }

    fun save(list: List<Bell>) {
        state.updateBells(list)
        BellScheduler.rescheduleAll(ctx)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Heading("Bells", 30)
                LText("Reminders to pray through the day", 15, Lc.muted)
            }
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Lc.accent)
                    .clickable(role = Role.Button, onClickLabel = "Add a bell") {
                        val id = (state.bells.maxOfOrNull { it.id } ?: 0) + 1
                        editing = Bell(id, "New bell", listOf(12 * 60), "tower", true, null)
                    },
                contentAlignment = Alignment.Center,
            ) { Ic(R.drawable.ic_plus, 20.dp, Lc.on) }
        }

        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 18.dp).padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!exactOk && Build.VERSION.SDK_INT >= 31) {
                LCard(padding = 16.dp) {
                    LText("Allow exact alarms so the bells ring on time.", 16)
                    Spacer(Modifier.height(10.dp))
                    OutlineButton("Open settings") {
                        runCatching {
                            ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                        }
                        exactOk = BellScheduler.canRingExactly(ctx)
                    }
                }
            }

            val nm = remember { ctx.getSystemService(android.app.NotificationManager::class.java) }
            if (Build.VERSION.SDK_INT >= 34 && state.bells.any { it.loud && it.enabled } && !nm.canUseFullScreenIntent()) {
                LCard(padding = 16.dp) {
                    LText("Allow Little Cloud to show bells over the lock screen.", 16)
                    Spacer(Modifier.height(10.dp))
                    OutlineButton("Open settings") {
                        runCatching {
                            ctx.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}")))
                        }
                    }
                }
            }

            LCard {
                state.bells.forEachIndexed { i, b ->
                    if (i > 0) HLine()
                    RowItem(Modifier.padding(start = 18.dp, end = 12.dp), onClick = { editing = b }) {
                        Column(Modifier.weight(1f).padding(vertical = 14.dp)) {
                            LText(b.name, 19)
                            Spacer(Modifier.height(3.dp))
                            LText(b.times.sorted().joinToString(" · ") { fmtTime(it) }, 16, if (b.enabled) Lc.ink else Lc.muted)
                            LText(Sounds.label(b.sound) + if (b.loud) " · rings through silent mode" else "", 14, Lc.muted)
                        }
                        LSwitch(b.enabled) { on -> save(state.bells.map { if (it.id == b.id) it.copy(enabled = on) else it }) }
                    }
                }
            }

            LCard {
                RowItem(Modifier.padding(start = 18.dp, end = 12.dp)) {
                    Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                        LText("Silence the bells in the Sacred Triduum", 17)
                        LText("From Holy Thursday to the Easter Vigil, a wooden clapper sounds instead", 14, Lc.muted)
                    }
                    LSwitch(state.triduum) { state.updateTriduum(it) }
                }
                HLine()
                RowItem(Modifier.padding(start = 18.dp, end = 12.dp), onClick = { play("clapper") }) {
                    LText("Hear the clapper", 16, Lc.accent, Modifier.weight(1f))
                    Ic(R.drawable.ic_play, 16.dp, Lc.accent)
                }
            }
            LText("Tap a bell to change its times or sound. Bells ring through the alarm volume.", 14, Lc.muted)
        }
    }

    editing?.let { b ->
        BellEditor(
            bell = b,
            isNew = state.bells.none { it.id == b.id },
            play = play,
            onDismiss = { play(null); editing = null },
            onDelete = { play(null); save(state.bells.filter { it.id != b.id }); editing = null },
            onSave = { nb ->
                play(null)
                save(if (state.bells.any { it.id == nb.id }) state.bells.map { if (it.id == nb.id) nb else it } else state.bells + nb)
                editing = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BellEditor(
    bell: Bell,
    isNew: Boolean,
    play: (String?) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (Bell) -> Unit,
) {
    var name by remember { mutableStateOf(bell.name) }
    var times by remember { mutableStateOf(bell.times.sorted()) }
    var sound by remember { mutableStateOf(bell.sound) }
    var loud by remember { mutableStateOf(bell.loud) }
    val editorCtx = LocalContext.current
    var picking by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri: Uri? = res.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) sound = "custom:$uri"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Lc.surface,
        title = { LText(if (isNew) "New bell" else "Edit bell", 22) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { LText("Name", 15) },
                    textStyle = TextStyle(fontFamily = LcFont, fontSize = 17.sp, color = Lc.ink),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Lc.accent, unfocusedBorderColor = Lc.line, cursorColor = Lc.accent),
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                )
                LText("Times", 14, Lc.accent, spacing = 0.12f, upper = true)
                times.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LText(fmtTime(t), 18, modifier = Modifier.weight(1f))
                        IconBtn(R.drawable.ic_close, "Remove ${fmtTime(t)}", size = 18.dp) { times = times - t }
                    }
                }
                OutlineButton("Add a time") { picking = true }

                LText("Sound", 14, Lc.accent, spacing = 0.12f, upper = true)
                Sounds.builtIn.forEach { s ->
                    SoundRow(s.label, s.note, sound == s.key, onSelect = { sound = s.key }, onPlay = { play(s.key) })
                }
                SoundRow(
                    "Choose from phone…",
                    if (sound.startsWith("custom:")) "Your ringtone is selected" else "Any ringtone or sound",
                    sound.startsWith("custom:"),
                    onSelect = {
                        picker.launch(
                            Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        )
                    },
                    onPlay = if (sound.startsWith("custom:")) ({ play(sound) }) else null,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LText("Ring like an alarm", 17)
                        LText("Sounds even when the phone is silenced and shows on the lock screen", 14, Lc.muted)
                    }
                    LSwitch(loud) { loud = it }
                }
                OutlineButton("Ring it now", Modifier.fillMaxWidth()) {
                    if (loud) {
                        play(null)
                        com.littlecloud.app.bells.BellService.start(editorCtx, name.ifBlank { "Bell" }, "This is how this bell will ring.", sound, null)
                    } else {
                        play(sound)
                    }
                }
                if (!isNew) {
                    TextButton(onClick = onDelete) { LText("Delete this bell", 16, Color(0xFFA33A2E)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(bell.copy(name = name.ifBlank { "Bell" }, times = times.sorted(), sound = sound, enabled = bell.enabled || isNew, loud = loud))
            }) { LText("Save", 16, Lc.accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { LText("Cancel", 16, Lc.muted) } },
    )

    if (picking) {
        val tp = rememberTimePickerState(7, 0, false)
        AlertDialog(
            onDismissRequest = { picking = false },
            containerColor = Lc.surface,
            confirmButton = {
                TextButton(onClick = {
                    val m = tp.hour * 60 + tp.minute
                    if (m !in times) times = (times + m).sorted()
                    picking = false
                }) { LText("Add", 16, Lc.accent) }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { LText("Cancel", 16, Lc.muted) } },
            text = { TimePicker(state = tp) },
        )
    }
}

@Composable
private fun SoundRow(label: String, note: String, selected: Boolean, onSelect: () -> Unit, onPlay: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).heightIn(min = 52.dp).selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (selected) Lc.accent else Lc.muted, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Box(Modifier.size(10.dp).clip(CircleShape).background(Lc.accent))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                LText(label, 17)
                LText(note, 14, Lc.muted)
            }
        }
        if (onPlay != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Lc.soft).clickable(role = Role.Button, onClickLabel = "Play $label", onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) { Ic(R.drawable.ic_play, 16.dp, Lc.accent) }
        }
    }
}
